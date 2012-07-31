
/* Windows CE 5 and 6 Helper Library
 *
 * This code is based on WCELIBCEX library by Mateusz Loskot (mateusz@loskot.net)
 * Copyright (c) 2006 Taxus SI Ltd.
 *
 */

#if defined(_WIN32_WCE)

#include <windows.h>
#include "wincehelper.h"

int _mkdir(const char *filename)
{
    int res;    
    size_t len;
    wchar_t *widestr;

    /* Covert filename buffer to Unicode. */
    len = MultiByteToWideChar (CP_ACP, 0, filename, -1, NULL, 0) ;
	widestr  = (wchar_t*)malloc(sizeof(wchar_t) * len);

    MultiByteToWideChar( CP_ACP, 0, filename, -1, widestr, len);
	
    /* Delete file using Win32 CE API call */
    res = CreateDirectory(widestr, NULL);
	
    /* Free wide-char string */
    free(widestr);

    if (res)
	    return 0; /* success */
    else
    {
        errno = GetLastError();
        return -1;
    }
}

int _rmdir(const char *filename)
{
    int res;    
    size_t len;
    wchar_t *widestr;

    /* Covert filename buffer to Unicode. */
	len = MultiByteToWideChar (CP_ACP, 0, filename, -1, NULL, 0) ;
	widestr = (wchar_t*)malloc(sizeof(wchar_t) * len);
	MultiByteToWideChar( CP_ACP, 0, filename, -1, widestr, len);
	
	/* Delete file using Win32 CE API call */
	res = RemoveDirectory(widestr);
	
	/* Free wide-char string */
	free(widestr);

    /* XXX - Consider following recommendations: */
    /* XXX - mloskot - update the st_ctime and st_mtime fields of the parent directory. */
    /* XXX - mloskot - set errno to [EEXIST] or [ENOTEMPTY] if function failed. */

    if (res)
	    return 0; /* success */
    else
        return -1;

}

int _unlink(const char *filename)
{
    int res;
    int len;
    wchar_t* pWideStr;

    /* Covert filename buffer to Unicode. */
    len = MultiByteToWideChar(CP_ACP, 0, filename, -1, NULL, 0) ;
    pWideStr = (wchar_t*)malloc(sizeof(wchar_t) * len);
	
    MultiByteToWideChar(CP_ACP, 0, filename, -1, pWideStr, len);
	
    /* Delete file using Win32 CE API call */
    res = DeleteFile(pWideStr);
	
    /* Free wide-char string */
    free(pWideStr);

    if (res)
        return 0; /* success */
    else
    {
        errno = GetLastError();
        return -1;
    }
}

char* getenv(const char* varname)
{
    return NULL;
}

static const int MONTHDAYS[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

static time_t __mktime_internal(struct tm *tmbuff, time_t _loctime_offset)
{
    time_t tres;
    int doy;
    int i;

    /* We do allow some ill-formed dates, but we don't do anything special
    with them and our callers really shouldn't pass them to us.  Do
    explicitly disallow the ones that would cause invalid array accesses
    or other algorithm problems. */
    if (tmbuff->tm_mon < 0 || tmbuff->tm_mon > 11 || tmbuff->tm_year < (EPOCH_YEAR - TM_YEAR_BASE))
    {
        return (time_t) -1;
    }

    /* Convert calender time to a time_t value. */
    tres = 0;

    /* Sum total amount of days from the Epoch with respect to leap years. */
    for (i = EPOCH_YEAR; i < tmbuff->tm_year + TM_YEAR_BASE; i++)
    {
        tres += 365 + IS_LEAP_YEAR(i);
    }

    /* Add days of months before current month. */
    doy = 0;
    for (i = 0; i < tmbuff->tm_mon; i++)
    {
        doy += MONTHDAYS[i];
    }
    tres += doy;
    
    /* Day of year */
    tmbuff->tm_yday = doy + tmbuff->tm_mday;

    if (tmbuff->tm_mon > 1 && IS_LEAP_YEAR(tmbuff->tm_year + TM_YEAR_BASE))
    {
        tres++;
    }
    
    /* Add days of current month and convert to total to hours. */
    tres = 24 * (tres + tmbuff->tm_mday - 1) + tmbuff->tm_hour;

    /* Add minutes part and convert total to minutes. */
    tres = 60 * tres + tmbuff->tm_min;

    /* Add seconds part and convert total to seconds. */
    tres = 60 * tres + tmbuff->tm_sec;
    
    /* For offset > 0 adjust time value for timezone
    given as local to UTC time difference in seconds). */
    tres += _loctime_offset;
    
    return tres;
}

time_t gmmktime(struct tm *tmbuff)
{
    return __mktime_internal(tmbuff, 0);
}

time_t time(time_t *timer)
{
    time_t t;
    struct tm tmbuff;
    SYSTEMTIME st;

    /* Retrive current system date time as UTC */
    GetSystemTime(&st);

    /* Build tm struct based on SYSTEMTIME values */

    /* Date values */
    tmbuff.tm_year = st.wYear - TM_YEAR_BASE;
    tmbuff.tm_mon = st.wMonth - 1;      /* wMonth value 1-12 */
    tmbuff.tm_mday = st.wDay;

    /* Time values */
    tmbuff.tm_hour = st.wHour;
    tmbuff.tm_min = st.wMinute;
    tmbuff.tm_sec = st.wSecond;
    tmbuff.tm_isdst = 0;    /* Always 0 for UTC time. */
    tmbuff.tm_wday = st.wDayOfWeek;
    tmbuff.tm_yday = 0;     /* Value is set by wceex_gmmktime */

    /* Convert tm struct to time_tUTC */
    t = gmmktime(&tmbuff);

    /* Assign time value. */
    if (timer != NULL)
    {
        *timer = t;
    }

	return t;
}

#endif  /* _WIN32_WCE */
