
/* Windows CE 5 and 6 Helper Library
 *
 * This code is based on WCELIBCEX library by Mateusz Loskot (mateusz@loskot.net)
 * Copyright (c) 2006 Taxus SI Ltd.
 *
 */

#if !defined(WINCEHELPER_H)
#define WINCEHELPER_H

#if defined(_WIN32_WCE)

#define vsnprintf _vsnprintf

#define EEXIST		ERROR_ALREADY_EXISTS
#define ENOENT		ERROR_FILE_NOT_FOUND
#define ENOTEMPTY	ERROR_DIR_NOT_EMPTY

#define TM_YEAR_BASE 1900    /* tm_year base year */

#define EPOCH_YEAR 1970
#define IS_LEAP_YEAR(year) \
    (((year) % 4) == 0 && (((year) % 100) != 0 || ((year) % 400) == 0))

typedef unsigned int size_t;

int errno;

#ifndef _TM_DEFINED
struct tm
{
	int tm_sec;     /* seconds after the minute - [0,59] */
	int tm_min;     /* minutes after the hour - [0,59] */
	int tm_hour;	/* hours since midnight - [0,23] */
	int tm_mday;	/* day of the month - [1,31] */
	int tm_mon;     /* months since January - [0,11] */
	int tm_year;	/* years since 1900 */
	int tm_wday;	/* days since Sunday - [0,6] */
	int tm_yday;	/* days since January 1 - [0,365] */
	int tm_isdst;	/* daylight savings time flag */
};
#define _TM_DEFINED
#endif /* _TM_DEFINED */

int _mkdir(const char *filename);
int _rmdir(const char *filename);
int _unlink(const char *filename);
static time_t __mktime_internal(struct tm *tmbuff, time_t _loctime_offset);
char* getenv(const char* varname);
time_t gmmktime(struct tm *tmbuff);
time_t time(time_t *timer);

#endif /* _WIN32_WCE */

#endif /* WINCEHELPER_H */
