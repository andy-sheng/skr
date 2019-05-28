#include <stdio.h>
#include <string.h>
#include "color_format_convert.h"

#define IS_ALIGNED(p, a) (!((uintptr_t)(p) & ((a)-1)))
#define SIMD_ALIGNED(var) var __attribute__((aligned(16)))
// Subsampled source needs to be increase by 1 of not even.
#define SS(width, shift) (((width) + (1 << (shift)) - 1) >> (shift))

// Any 1 to 1.
#define ANY11(NAMEANY, ANY_SIMD, UVSHIFT, SBPP, BPP, MASK)                \
  void NAMEANY(const uint8_t* src_ptr, uint8_t* dst_ptr, int width) {     \
    SIMD_ALIGNED(uint8_t temp[128 * 2]);                                  \
    memset(temp, 0, 128); /* for YUY2 and msan */                         \
    int r = width & MASK;                                                 \
    int n = width & ~MASK;                                                \
    if (n > 0) {                                                          \
      ANY_SIMD(src_ptr, dst_ptr, n);                                      \
    }                                                                     \
    memcpy(temp, src_ptr + (n >> UVSHIFT) * SBPP, SS(r, UVSHIFT) * SBPP); \
    ANY_SIMD(temp, temp + 128, MASK + 1);                                 \
    memcpy(dst_ptr + n * BPP, temp + 128, r * BPP);                       \
  }

// Any 1 to 2 with source stride (2 rows of source).  Outputs UV planes.
// 128 byte row allows for 32 avx ARGB pixels.
#define ANY12S(NAMEANY, ANY_SIMD, UVSHIFT, BPP, MASK)                        \
  void NAMEANY(const uint8_t* src_ptr, int src_stride_ptr, uint8_t* dst_u,   \
               uint8_t* dst_v, int width) {                                  \
    SIMD_ALIGNED(uint8_t temp[128 * 4]);                                     \
    memset(temp, 0, 128 * 2); /* for msan */                                 \
    int r = width & MASK;                                                    \
    int n = width & ~MASK;                                                   \
    if (n > 0) {                                                             \
      ANY_SIMD(src_ptr, src_stride_ptr, dst_u, dst_v, n);                    \
    }                                                                        \
    memcpy(temp, src_ptr + (n >> UVSHIFT) * BPP, SS(r, UVSHIFT) * BPP);      \
    memcpy(temp + 128, src_ptr + src_stride_ptr + (n >> UVSHIFT) * BPP,      \
           SS(r, UVSHIFT) * BPP);                                            \
    if ((width & 1) && UVSHIFT == 0) { /* repeat last pixel for subsample */ \
      memcpy(temp + SS(r, UVSHIFT) * BPP, temp + SS(r, UVSHIFT) * BPP - BPP, \
             BPP);                                                           \
      memcpy(temp + 128 + SS(r, UVSHIFT) * BPP,                              \
             temp + 128 + SS(r, UVSHIFT) * BPP - BPP, BPP);                  \
    }                                                                        \
    ANY_SIMD(temp, 128, temp + 256, temp + 384, MASK + 1);                   \
    memcpy(dst_u + (n >> 1), temp + 256, SS(r, 1));                          \
    memcpy(dst_v + (n >> 1), temp + 384, SS(r, 1));                          \
  }


#if defined(__arm__) && defined(__ARM_NEON__)

void YUVAToYRow_NEON(const uint8_t* src_yuva, uint8_t* dst_y, int width) {
    __asm volatile(
        "1:                                            \n"
            "vld4.8     {d0, d1, d2, d3}, [%0]!        \n"  // load 8 pixels of YUVA.
            "subs       %2, %2, #8                     \n"  // 8 processed per loop.
            "vst1.8     {d0}, [%1]!                    \n"  // store 8 pixels Y.
            "bgt        1b                             \n"
        :   "+r"(src_yuva),  // %0
            "+r"(dst_y),     // %1
            "+r"(width)      // %2
        :
        :   "cc", "memory", "d0", "d1", "d2", "d3");
}

void YUVAToUVRow_NEON(const uint8_t* src_yuva, int src_stride_yuva,
                      uint8_t* dst_u, uint8_t* dst_v, int width) {
    __asm volatile (
            "add        %1, %0, %1                     \n"  // src_stride + src_yuva
        "1:                                            \n"
            "vld4.8     {d0, d2, d4, d6}, [%0]!        \n"  // load 8 YUVA pixels.
            "vld4.8     {d1, d3, d5, d7}, [%0]!        \n"  // load next 8 YUVA pixels.
            "vpaddl.u8  q2, q2                         \n"  // V 16 bytes -> 8 shorts.
            "vpaddl.u8  q1, q1                         \n"  // U 16 bytes -> 8 shorts.
            "vld4.8     {d8, d10, d12, d14}, [%1]!     \n"  // load 8 more YUVA pixels.
            "vld4.8     {d9, d11, d13, d15}, [%1]!     \n"  // load last 8 YUVA pixels.
            "vpadal.u8  q2, q6                         \n"  // V 16 bytes -> 8 shorts.
            "vpadal.u8  q1, q5                         \n"  // U 16 bytes -> 8 shorts.

            "vqshrn.u16  d0, q1, #2                    \n"  // 4x average
            "vqshrn.u16  d1, q2, #2                    \n"
            "subs       %4, %4, #16                    \n"  // 32 processed per loop.
            "vst1.8     {d0}, [%2]!                    \n"  // store 8 pixels U.
            "vst1.8     {d1}, [%3]!                    \n"  // store 8 pixels V.
            "bgt        1b                             \n"
        :   "+r"(src_yuva),       // %0
            "+r"(src_stride_yuva),// %1
            "+r"(dst_u),          // %2
            "+r"(dst_v),          // %3
            "+r"(width)           // %4
        :
        :   "cc", "memory", "q0", "q1", "q2", "q3", "q4", "q5", "q6", "q7"
    );
}

#elif defined(__aarch64__)

void YUVAToYRow_NEON(const uint8_t* src_yuva, uint8_t* dst_y, int width) {
    __asm volatile(
        "1:                                            \n"
            "ld4        {v0.8b,v1.8b,v2.8b,v3.8b}, [%0], #32 \n"  // load 8 pixels.
            "subs       %w2, %w2, #8                   \n"  // 8 processed per loop.
            "st1        {v0.8b}, [%1], #8              \n"  // store 8 pixels Y.
            "b.gt       1b                             \n"
        :   "+r"(src_yuva),  // %0
            "+r"(dst_y),     // %1
            "+r"(width)      // %2
        :
        :   "cc", "memory", "v0", "v1", "v2", "v3");
}

void YUVAToUVRow_NEON(const uint8_t* src_yuva, int src_stride_yuva, uint8_t* dst_u,
                 uint8_t* dst_v, int width) {
    const uint8_t* src_yuva_1 = src_yuva + src_stride_yuva;
    __asm volatile (
        "1:                                            \n"
            "ld4        {v0.16b,v1.16b,v2.16b,v3.16b}, [%0], #64 \n"  // load 16 pixels.
            "uaddlp     v3.8h, v2.16b                  \n"  // V 16 bytes -> 8 shorts.
            "uaddlp     v2.8h, v1.16b                  \n"  // U 16 bytes -> 8 shorts.
            "ld4        {v4.16b,v5.16b,v6.16b,v7.16b}, [%1], #64 \n"  // load 16 more.
            "uadalp     v3.8h, v6.16b                  \n"  // V 16 bytes -> 8 shorts.
            "uadalp     v2.8h, v5.16b                  \n"  // U 16 bytes -> 8 shorts.

            "uqshrn     v0.8b, v2.8h, #2               \n"  // 4x average
            "uqshrn     v1.8b, v3.8h, #2               \n"

            "subs       %w4, %w4, #16                  \n"  // 32 processed per loop.
            "st1        {v0.8b}, [%2], #8              \n"  // store 8 pixels U.
            "st1        {v1.8b}, [%3], #8              \n"  // store 8 pixels V.
            "b.gt       1b                             \n"
        :   "+r"(src_yuva),       // %0
            "+r"(src_yuva_1),     // %1
            "+r"(dst_u),          // %2
            "+r"(dst_v),          // %3
            "+r"(width)           // %4
        :
        :   "cc", "memory", "v0", "v1", "v2", "v3", "v4", "v5", "v6", "v7"
    );
}

#endif

#if (defined(__arm__) && defined(__ARM_NEON__)) || defined(__aarch64__)

ANY11(YUVAToYRow_Any_NEON, YUVAToYRow_NEON, 0, 4, 1, 7)
ANY12S(YUVAToUVRow_Any_NEON, YUVAToUVRow_NEON, 0, 4, 15)

#endif

void YUVAToYRow_C(const uint8_t* src_yuva, uint8_t* dst_y, int width) {
    for (int i = 0; i < width; i++) {
        dst_y[i] = src_yuva[0];
        src_yuva += 4;
    }
}

void YUVAToUVRow_C(const uint8_t* src_yuva, int src_stride_yuva, uint8_t* dst_u,
                 uint8_t* dst_v, int width) {
    const uint8_t* src_yuva1 = src_yuva + src_stride_yuva;
    for (int i = 0; i < width - 1; i += 2) {
        dst_u[0] = (src_yuva[1] + src_yuva[5] + src_yuva1[1] + src_yuva1[5]) >> 2;
        dst_v[0] = (src_yuva[2] + src_yuva[6] + src_yuva1[2] + src_yuva1[6]) >> 2;
        src_yuva += 8;
        src_yuva1 += 8;
        dst_u += 1;
        dst_v += 1;
    }
}

int YUVAToI420(const uint8_t* src_yuva,
               int src_stride_yuva,
               uint8_t* dst_y,
               int dst_stride_y,
               uint8_t* dst_u,
               int dst_stride_u,
               uint8_t* dst_v,
               int dst_stride_v,
               int width,
               int height) {
    void (*YUVAToYRow)(const uint8_t* src_abgr, uint8_t* dst_y, int width) = YUVAToYRow_C;
    void (*YUVAToUVRow)(const uint8_t* src_abgr0, int src_stride_abgr, uint8_t* dst_u,
                        uint8_t* dst_v, int width) = YUVAToUVRow_C;
    if (!src_yuva || !dst_y || !dst_u || !dst_v || width <= 0 || height == 0) {
        return -1;
    }
    // Negative height means invert the image.
    if (height < 0) {
        height = -height;
        src_yuva = src_yuva + (height - 1) * src_stride_yuva;
        src_stride_yuva = -src_stride_yuva;
    }

#if (defined(__arm__) && defined(__ARM_NEON__)) || defined(__aarch64__)
    YUVAToYRow = YUVAToYRow_Any_NEON;
    YUVAToUVRow = YUVAToUVRow_Any_NEON;
    if (IS_ALIGNED(src_stride_yuva / 4, 16)) {
        YUVAToYRow = YUVAToYRow_NEON;
        YUVAToUVRow = YUVAToUVRow_NEON;
    } else if (IS_ALIGNED(src_stride_yuva / 4, 8)) {
        YUVAToYRow = YUVAToYRow_NEON;
    }
#endif

    for (int y = 0; y < height - 1; y += 2) {
        YUVAToUVRow(src_yuva, src_stride_yuva, dst_u, dst_v, width);
        YUVAToYRow(src_yuva, dst_y, width);
        YUVAToYRow(src_yuva + src_stride_yuva, dst_y + dst_stride_y, width);
        src_yuva += src_stride_yuva * 2;
        dst_y += dst_stride_y * 2;
        dst_u += dst_stride_u;
        dst_v += dst_stride_v;
    }
    if (height & 1) {
        YUVAToUVRow(src_yuva, 0, dst_u, dst_v, width);
        YUVAToYRow(src_yuva, dst_y, width);
    }
    return 0;
}