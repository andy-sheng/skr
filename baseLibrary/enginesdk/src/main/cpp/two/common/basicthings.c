#include <assert.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#include "functions.h"
#include "defines.h"

#define SKR_FILENUM 50 
#define SKR_FILENAMELEN 150
FILE *gSKRfile[SKR_FILENUM] = { 0 };
char gSKRfname[SKR_FILENUM][SKR_FILENAMELEN] = { 0 };
char gSKRfname_haveno[SKR_FILENUM][SKR_FILENAMELEN] = { 0 };
char gSKRfname_have[SKR_FILENUM][SKR_FILENAMELEN] = { 0 };


int fwrite_SKR(const void *towrite, int sizeofelement,int thecount, const char *filename)
{	
	int i;
	int filehasopened = 0;
    int tmp = 0;

	for (i = 0; i < SKR_FILENUM; i++)
	{
		if (!strcmp(filename, gSKRfname[i]))
		{
			tmp = fwrite(towrite, sizeofelement, thecount, gSKRfile[i]);
			filehasopened = 1;
			break;
		}
	}
	if (!filehasopened)
	{
		for (i = 0; i < SKR_FILENUM; i++)
		{
			if (gSKRfname[i][0] == 0)
			{
				strcpy(gSKRfname[i], filename);
				gSKRfile[i] = fopen(filename, "wb");
				tmp = fwrite(towrite, sizeofelement, thecount, gSKRfile[i]);
				break;
			}
		}
	}
    return tmp;
}
int fread_SKR(void *dst, int sizeofelement,int thecount, const char *filename)
{	
	int i;
	int filehasopened = 0;
	int freadlen = 0;

	for (i = 0; i < SKR_FILENUM; i++)
	{
		if (!strcmp(filename, gSKRfname[i]))
		{
			freadlen = fread(dst,sizeofelement, thecount, gSKRfile[i]);
			filehasopened = 1;
			break;
		}
	}
	if (!filehasopened)
	{
		for (i = 0; i < SKR_FILENUM; i++)
		{
			if (gSKRfname[i][0] == 0)
			{
				strcpy(gSKRfname[i], filename);
				gSKRfile[i] = fopen(filename, "rb");
				freadlen = fread(dst, sizeofelement, thecount, gSKRfile[i]);
				break;
			}
		}
	}
	return freadlen;
}



void fclose_SKR(const char *filename)
{	
	int i;
	int j;

	for (i = 0; i < SKR_FILENUM; i++)
	{
		if (!strcmp(filename, gSKRfname[i]))
		{
			if (gSKRfile[i])
			{
				fclose(gSKRfile[i]);
				gSKRfile[i] = NULL;
				for (j=0;j<SKR_FILENAMELEN;j++)
				{
					gSKRfname[i][j] = 0;
				}
			}
			break;
		}
	}
}
int fwrite_t_SKR(const void *towrite, int sizeofelement, int thecount, const char *filename,short line)
{
	int i;
	int filehasopened = 0;
	char filename_time[SKR_FILENAMELEN];
	char thetime[20];
	short pcmline[SKR_MAX_FRAME_SAMPLE_STEREO];
	time_t tt;
	struct tm *t;
    int tmp = 0;
    const void *towrite2;

	tt = time(NULL);
	t = localtime(&tt);
	

	if (towrite == NULL)
	{
		if (sizeofelement == 2)
		{
			for (i = 0; i < thecount; i++)
			{
				pcmline[i] = line;
			}
		} 
		else 
		{
			assert(0);
			return 0;
		}
		towrite2 = pcmline;
	}
	else
	{
		towrite2 = towrite;
	}
	
	for (i = 0; i < SKR_FILENUM; i++)
	{
		if (!strcmp(filename, gSKRfname[i]))
		{
			tmp = fwrite(towrite2, sizeofelement, thecount, gSKRfile[i]);
			filehasopened = 1;
			break;
		}
	}
	if (!filehasopened)
	{
		for (i = 0; i < SKR_FILENUM; i++)
		{
			if (gSKRfname[i][0] == 0)
			{
				strcpy(gSKRfname[i], filename);
				sprintf(thetime, "%02d-%02d-%02d", t->tm_hour, t->tm_min, t->tm_sec);
				strcpy(filename_time, filename);
				strcat(filename_time, thetime);//gaiweihouzhui
				strcat(filename_time, ".pcm");//gaiweihouzhui
				gSKRfile[i] = fopen(filename_time, "wb");
				tmp = fwrite(towrite2, sizeofelement, thecount, gSKRfile[i]);
				break;
			}
		}
	}
    return tmp;
}
void fcloseall_SKR()
{
	int i,j;

	for (i = 0; i < SKR_FILENUM; i++)
	{
		if (gSKRfile[i])
		{
			fclose(gSKRfile[i]);
		}
		for (j = 0; j < SKR_FILENAMELEN;j++)
		{
			gSKRfname[i][j] = 0;
			gSKRfname_haveno[i][j] = 0;
			gSKRfname_have[i][j] = 0;
		}
	}

}

int findfile_SKR(const char *filename)
{
	FILE *tempFile = NULL;
	int i;

	for (i = 0; i < SKR_FILENUM; i++)
	{
		if (!strcmp(filename, gSKRfname_haveno[i]))
		{
			return 0;//no this file!
		}
		else if(!strcmp(filename, gSKRfname_have[i]))
		{
			return 1;//have this file!
		}
	}

	tempFile = fopen(filename,"rb");
	if (tempFile)
	{
		fclose(tempFile);
		for (i = 0; i < SKR_FILENUM; i++)
		{
			if (gSKRfname_have[i][0] == 0)
			{
				strcpy(gSKRfname_have[i], filename);
				return 1;
			}
		}
	}
	else
	{
		for (i = 0; i < SKR_FILENUM; i++)
		{
			if (gSKRfname_haveno[i][0] == 0)
			{
				strcpy(gSKRfname_haveno[i], filename);
				return 0;
			}
		}
	}
	return 0;
}
char gappfilepath_SKR[120] = {0};

int fwrite_findordef(const void *towrite, int sizeofelement, int thecount, const char *filename, short line,int hasdefine)
{
	char pathfilename[SKR_FILENAMELEN];

	strcpy(pathfilename,gappfilepath_SKR);
	strcat(pathfilename,filename);

	if (findfile_SKR(pathfilename)||hasdefine)
	{
		return fwrite_t_SKR(towrite, sizeofelement, thecount, pathfilename, line);
	}
	else
	{
		return -54321;
	}
}

int findfile(const char *filename)
{
	char pathfilename[SKR_FILENAMELEN];

	strcpy(pathfilename, gappfilepath_SKR);
	strcat(pathfilename, filename);

	return findfile_SKR(pathfilename);
}
int fwritefile(const void *towrite, int sizeofelement, int thecount, const char *filename, short line)
{
	char pathfilename[SKR_FILENAMELEN];

	strcpy(pathfilename, gappfilepath_SKR);
	strcat(pathfilename, filename);

	return fwrite_t_SKR(towrite, sizeofelement, thecount, pathfilename, line);

}
int freadfile(void *dst, int sizeofelement, int thecount, const char *filename)
{
	char pathfilename[SKR_FILENAMELEN];

	strcpy(pathfilename, gappfilepath_SKR);
	strcat(pathfilename, filename);
	return fread_SKR(dst, sizeofelement, thecount, pathfilename);
}

WavHead gSKRwavhead[SKR_FILENUM];
int freadwavhead(int *ch,int *sr,int *bitpersample, char *filename)
{
	int i;
	char tmp[300];
	char finddata[4];
	char *p,*p2,*p3;
	char charx;
	int k;
	char bigchunk[10000];
	int factsize;
	unsigned int pcmsize;
	int bigchunktail;

	for (i = 0; i < SKR_FILENUM; i++)
	{
		if (gSKRfname[i][0] == 0)
		{
			strcpy(gSKRfname[i], filename);
			gSKRfile[i] = fopen(filename, "rb");
			fread(&gSKRwavhead[i], sizeof(WavHead), 1, gSKRfile[i]);
			*ch = gSKRwavhead[i].NChannels;
			*sr = gSKRwavhead[i].SampleRate;
			*bitpersample = gSKRwavhead[i].NBitsPersample;

			if (gSKRwavhead[i].BytesPerSample !=gSKRwavhead[i].NChannels*gSKRwavhead[i].NBitsPersample / 8)
			{
				return 2;
			}
			if (gSKRwavhead[i].FormatCategory != 1 && gSKRwavhead[i].FormatCategory != -2 )
			{
				return 2;
			}

			//check valid
			if (gSKRwavhead[i].RIFF[0] != 'R')
			{
				return 2;
			}
			if (gSKRwavhead[i].RIFF[1] != 'I')
			{
				return 2;
			}
			if (gSKRwavhead[i].RIFF[2] != 'F')
			{
				return 2;
			}
			if (gSKRwavhead[i].RIFF[3] != 'F')
			{
				return 2;
			}
			if (gSKRwavhead[i].WAVEfmt_[0] != 'W')
			{
				return 2;
			}
			if (gSKRwavhead[i].WAVEfmt_[1] != 'A')
			{
				return 2;
			}
			if (gSKRwavhead[i].WAVEfmt_[2] != 'V')
			{
				return 2;
			}
			if (gSKRwavhead[i].WAVEfmt_[3] != 'E')
			{
				return 2;
			}
			if (gSKRwavhead[i].WAVEfmt_[4] != 'f')
			{
				return 2;
			}
			if (gSKRwavhead[i].WAVEfmt_[5] != 'm')
			{
				return 2;
			}
			if (gSKRwavhead[i].WAVEfmt_[6] != 't')
			{
				return 2;
			}
			if (gSKRwavhead[i].WAVEfmt_[7] != ' ')
			{
				return 2;
			}


			p = &gSKRwavhead[i].data[0];

			for (k = 0; k < 8; k++)
			{
				bigchunk[k] = p[k];
			}

			for (k=0;k<gSKRwavhead[i].SizeofPCMWAVEFORMAT - 16;k++)
			{
				fread(bigchunk+8+k, 1, 1, gSKRfile[i]);//wrong! there may be "fact"
			}
			bigchunktail = 8 + k;//bigchunk[bigchunktail] is empty
			if(bigchunk[bigchunktail - 8] == 'd'
			&& bigchunk[bigchunktail - 8 + 1] == 'a'
			&& bigchunk[bigchunktail - 8 + 2] == 't'
			&& bigchunk[bigchunktail - 8 + 3] == 'a')
			{
				p3 = &bigchunk[bigchunktail - 8 + 4];
				pcmsize = ((unsigned int *)p3)[0];
				return 0;
			}
			else
			{
				p2 = &bigchunk[bigchunktail - 8 + 4];
				factsize = ((int *)p2)[0];
				fread(bigchunk + bigchunktail, 1, factsize + 8, gSKRfile[i]);
				bigchunktail += factsize + 8;
				if (bigchunk[bigchunktail - 8] == 'd'
					&& bigchunk[bigchunktail - 8 + 1] == 'a'
					&& bigchunk[bigchunktail - 8 + 2] == 't'
					&& bigchunk[bigchunktail - 8 + 3] == 'a')
				{
					p3 = &bigchunk[bigchunktail - 8 + 4];
					pcmsize = ((unsigned int *)p3)[0];
					return 0;
				}
				else
				{
					return 2;
				}
			}

			//if (gSKRwavhead[i].data[0] != 'd')
			//{
			//	return 2;
			//}
			//if (gSKRwavhead[i].data[1] != 'a')
			//{
			//	return 2;
			//}
			//if (gSKRwavhead[i].data[2] != 't')
			//{
			//	return 2;
			//}
			//if (gSKRwavhead[i].data[3] != 'a')
			//{
			//	return 2;
			//}

			//fread(tmp, 1, gSKRwavhead[i].SizeofPCMWAVEFORMAT-16, gSKRfile[i]);//wrong! there may be "fact"
			//assert(16 == gSKRwavhead[i].NBitsPersample);
			//assert(1 == gSKRwavhead[i].FormatCategory);
			return 0;
			//break;
		}
	}
	return 1;
}
//after readwavhead we can use freadSKR to read pcm from wav
void fwritewavpcm(short *pcm,int len,char *filename)
{
	int i;
	int filehasopened = 0;

	for (i = 0; i < SKR_FILENUM; i++)
	{
		if (!strcmp(filename, gSKRfname[i]))
		{
			fwrite(pcm, sizeof(short), len, gSKRfile[i]);
			gSKRwavhead[i].RawDataFileLength += len * 2;
			filehasopened = 1;
			break;
		}
	}
	if (!filehasopened)
	{
		for (i = 0; i < SKR_FILENUM; i++)
		{
			if (gSKRfname[i][0] == 0)
			{
				strcpy(gSKRfname[i], filename);
				gSKRfile[i] = fopen(filename, "wb");
				fwrite(&gSKRwavhead[i],sizeof(WavHead),1, gSKRfile[i]);
				gSKRwavhead[i].RawDataFileLength = 0;
				fwrite(pcm, sizeof(short), len, gSKRfile[i]);
				gSKRwavhead[i].RawDataFileLength += len * 2;
				break;
			}
		}
	}
}
void fwritewavpcm_t(short *pcm, int len, char *filename)
{
	int i;
	int filehasopened = 0;
	char filename_time[SKR_FILENAMELEN];
	time_t tt;
	struct tm *t;

	tt = time(NULL);
	t = localtime(&tt);

	for (i = 0; i < SKR_FILENUM; i++)
	{
		if (!strcmp(filename, gSKRfname[i]))
		{
			fwrite(pcm, sizeof(short), len, gSKRfile[i]);
			gSKRwavhead[i].RawDataFileLength += len * 2;
			filehasopened = 1;
			break;
		}
	}
	if (!filehasopened)
	{
		for (i = 0; i < SKR_FILENUM; i++)
		{
			if (gSKRfname[i][0] == 0)
			{
				strcpy(gSKRfname[i], filename);
				sprintf(filename_time, "%02d-%02d-%02d-", t->tm_hour, t->tm_min, t->tm_sec);
				strcat(filename_time, filename);
				gSKRfile[i] = fopen(filename_time, "wb");
				fwrite(&gSKRwavhead[i], sizeof(WavHead), 1, gSKRfile[i]);
				gSKRwavhead[i].RawDataFileLength = 0;
				fwrite(pcm, sizeof(short), len, gSKRfile[i]);
				gSKRwavhead[i].RawDataFileLength += len * 2;
				break;
			}
		}
	}
}
void fclosewav(int ch, int sr, char *filename)
{
	int i;
	int j;

	for (i = 0; i < SKR_FILENUM; i++)
	{
		if (!strcmp(filename, gSKRfname[i]))
		{
			if (gSKRfile[i])
			{
				rewind(gSKRfile[i]);
				strcpy(gSKRwavhead[i].RIFF, "RIFF");//Resource Interchange File Flag (0-3) "RIFF"
				gSKRwavhead[i].FileLength = gSKRwavhead[i].RawDataFileLength + 44 -8;//File Length ( not include 8 bytes from the beginning ) (4-7)//RawDataFileLength + 44 - 8;
				strcpy(gSKRwavhead[i].WAVEfmt_,"WAVEfmt ");//WAVE File Flag (8-15) "WAVEfmt "
				gSKRwavhead[i].SizeofPCMWAVEFORMAT = 16;//Transitory Byte ( normally it is 10H 00H 00H 00H ) (16-19) //Sizeof(PCMWAVEFORMAT)
				gSKRwavhead[i].FormatCategory = 1;//Format Category ( normally it is 1 means PCM-u Law ) (20-21)
				gSKRwavhead[i].NChannels = ch;//NChannels (22-23)
				gSKRwavhead[i].SampleRate = sr;//Sample Rate (24-27)//nSamplesPerSec
				gSKRwavhead[i].SampleBytes = ch*sr*2;//nAvgBytesperSec//l=NChannels*SampleRate*NBitsPersample/8 (28-31)
				gSKRwavhead[i].BytesPerSample = ch*2;//nBlockAlign//i=NChannels*NBitsPersample/8 (32-33)
				gSKRwavhead[i].NBitsPersample = 16;//NBitsPersample (34-35)
				strcpy(gSKRwavhead[i].data,"data");//Data Flag (36-39) "data"
				//long RawDataFileLength;//Raw Data File Length (41-43) 
				fwrite(&gSKRwavhead[i], sizeof(WavHead), 1, gSKRfile[i]);
				fclose(gSKRfile[i]);
				gSKRfile[i] = NULL;
				for (j = 0; j < SKR_FILENAMELEN; j++)
				{
					gSKRfname[i][j] = 0;
				}
			}
			break;
		}
	}
}
