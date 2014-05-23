#include "stdlib.h"
#include "io.h"
#include "sys\stat.h"

#define vsnprintf _vsnprintf

#define GETTIMEOFDAY

int rename(const char *oldfile, const char *newfile);
int _unlink(const char *filename);
#define remove _unlink

