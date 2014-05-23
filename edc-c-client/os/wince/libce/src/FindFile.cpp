#include <windows.h>

HANDLE FindFirstFileA( LPCSTR lpFileName, LPWIN32_FIND_DATAA lpFindFileDataA )
{
  WCHAR wszFileName[MAX_PATH+1];
  WIN32_FIND_DATA FileDataW;

  MultiByteToWideChar( CP_ACP, 0, lpFileName, -1, wszFileName, MAX_PATH );
  HANDLE hSrchFile = FindFirstFile( wszFileName, &FileDataW );
  if( lpFindFileDataA )
  {
    lpFindFileDataA->dwFileAttributes = FileDataW.dwFileAttributes;
    lpFindFileDataA->nFileSizeHigh    = FileDataW.nFileSizeHigh;
    lpFindFileDataA->nFileSizeLow     = FileDataW.nFileSizeLow;
    lpFindFileDataA->dwReserved0      = 0;
    lpFindFileDataA->dwReserved1      = 0;
    memcpy( &lpFindFileDataA->ftCreationTime, &FileDataW.ftCreationTime, sizeof(FILETIME) );
    memcpy( &lpFindFileDataA->ftLastAccessTime, &FileDataW.ftLastAccessTime, sizeof(FILETIME) );
    memcpy( &lpFindFileDataA->ftLastWriteTime, &FileDataW.ftLastWriteTime, sizeof(FILETIME) );
    WideCharToMultiByte( CP_ACP, 0, FileDataW.cFileName, wcslen(FileDataW.cFileName), lpFindFileDataA->cFileName, MAX_PATH, NULL, NULL );
    // lpFindFileDataA->cAlternateFileName - TODO? filename in 8.3 format
  }

  return hSrchFile;
}


BOOL FindNextFileA( HANDLE hFindFile, LPWIN32_FIND_DATAA lpFindFileDataA )
{
  BOOL fResult = FALSE;
  WIN32_FIND_DATA FileDataW;

  if( lpFindFileDataA )
  {
    FileDataW.dwFileAttributes    = lpFindFileDataA->dwFileAttributes;
    FileDataW.nFileSizeHigh       = lpFindFileDataA->nFileSizeHigh;
    FileDataW.nFileSizeLow        = lpFindFileDataA->nFileSizeLow;
    FileDataW.dwOID               = 0;  // ???
    memcpy( &FileDataW.ftCreationTime, &lpFindFileDataA->ftCreationTime, sizeof(FILETIME) );
    memcpy( &FileDataW.ftLastAccessTime, &lpFindFileDataA->ftLastAccessTime, sizeof(FILETIME) );
    memcpy( &FileDataW.ftLastWriteTime, &lpFindFileDataA->ftLastWriteTime, sizeof(FILETIME) );
    MultiByteToWideChar( CP_ACP, 0, lpFindFileDataA->cFileName, -1, FileDataW.cFileName, MAX_PATH );

    fResult = FindNextFile( hFindFile, &FileDataW );

    lpFindFileDataA->dwFileAttributes = FileDataW.dwFileAttributes;
    lpFindFileDataA->nFileSizeHigh    = FileDataW.nFileSizeHigh;
    lpFindFileDataA->nFileSizeLow     = FileDataW.nFileSizeLow;
    lpFindFileDataA->dwReserved0      = 0;
    lpFindFileDataA->dwReserved1      = 0;
    memcpy( &lpFindFileDataA->ftCreationTime, &FileDataW.ftCreationTime, sizeof(FILETIME) );
    memcpy( &lpFindFileDataA->ftLastAccessTime, &FileDataW.ftLastAccessTime, sizeof(FILETIME) );
    memcpy( &lpFindFileDataA->ftLastWriteTime, &FileDataW.ftLastWriteTime, sizeof(FILETIME) );
    WideCharToMultiByte( CP_ACP, 0, FileDataW.cFileName, wcslen(FileDataW.cFileName), lpFindFileDataA->cFileName, MAX_PATH, NULL, NULL );
    // lpFindFileDataA->cAlternateFileName - TODO? filename in 8.3 format
  }

  return fResult;
}
