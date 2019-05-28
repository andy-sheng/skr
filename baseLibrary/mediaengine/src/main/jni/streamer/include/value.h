
#ifndef ANDROID_FILTERFW_CORE_VALUE_H
#define ANDROID_FILTERFW_CORE_VALUE_H

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
  void* value;
  int type;
  int count;
} Value;

int GetIntValue(Value value);
float GetFloatValue(Value value);
const char* GetStringValue(Value value);
const char* GetBufferValue(Value value);
char* GetMutableBufferValue(Value value);
int* GetIntArrayValue(Value value);
float* GetFloatArrayValue(Value value);

int ValueIsNull(Value value);
int ValueIsInt(Value value);
int ValueIsFloat(Value value);
int ValueIsString(Value value);
int ValueIsBuffer(Value value);
int ValueIsMutableBuffer(Value value);
int ValueIsIntArray(Value value);
int ValueIsFloatArray(Value value);

Value MakeNullValue();
Value MakeIntValue(int value);
Value MakeFloatValue(float value);
Value MakeStringValue(const char* value);
Value MakeBufferValue(const char* data, int size);
Value MakeBufferValueNoCopy(const char* data, int size);
Value MakeMutableBufferValue(const char* data, int size);
Value MakeMutableBufferValueNoCopy(char* data, int size);
Value MakeIntArrayValue(const int* values, int count);
Value MakeFloatArrayValue(const float* values, int count);

int SetIntValue(Value* value, int new_value);
int SetFloatValue(Value* value, float new_value);
int SetStringValue(Value* value, const char* new_value);
int SetMutableBufferValue(Value* value, const char* new_data, int size);
int SetIntArrayValue(Value* value, const int* new_values, int count);
int SetFloatArrayValue(Value* value, const float* new_values, int count);

int GetValueCount(Value value);

void ReleaseValue(Value* value);

#ifdef __cplusplus
} // extern "C"
#endif

#endif
