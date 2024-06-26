# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

project(cr3engine)

set(CR3_ROOT ${PROJECT_SOURCE_DIR}/../..)

# thirdparty sources definitions
include(${CR3_ROOT}/thirdparty_repo/repo_srcdirs.cmake)

include_directories(${CR3_ROOT}/crengine/include)
include_directories(${CR3_ROOT}/thirdparty/${REPO_LIBPNG_SRCDIR})
include_directories(thirdparty_libs/libpng/lib)
include_directories(${CR3_ROOT}/thirdparty/${REPO_FREETYPE_SRCDIR})
include_directories(${CR3_ROOT}/thirdparty/${REPO_FREETYPE_SRCDIR}/include)
include_directories(thirdparty_libs/freetype)
include_directories(${CR3_ROOT}/thirdparty/${REPO_HARFBUZZ_SRCDIR}/src)
include_directories(${CR3_ROOT}/thirdparty/${REPO_LIBJPEG_SRCDIR})
include_directories(${CR3_ROOT}/thirdparty_unman/antiword)
include_directories(${CR3_ROOT}/thirdparty_unman/chmlib/src)
include_directories(${CR3_ROOT}/thirdparty_unman/nanosvg/src)
include_directories(${CR3_ROOT}/thirdparty/${REPO_FRIBIDI_SRCDIR}/lib)
include_directories(thirdparty_libs/fribidi/lib)
include_directories(${CR3_ROOT}/thirdparty/${REPO_LIBUNIBREAK_SRCDIR}/src)
include_directories(${CR3_ROOT}/thirdparty/${REPO_ZSTD_SRCDIR}/lib)
include_directories(${CR3_ROOT}/thirdparty/${REPO_UTF8PROC_SRCDIR})

add_definitions(-DLINUX=1 -D_LINUX=1)
add_definitions(-DFT_CONFIG_MODULES_H=<android/config/ftmodule.h> -DFT_CONFIG_OPTIONS_H=<android/config/ftoption.h>)
add_definitions(-DDOC_DATA_COMPRESSION_LEVEL=1 -DDOC_BUFFER_SIZE=0x1000000)
add_definitions(-DENABLE_CACHE_FILE_CONTENTS_VALIDATION=1)
add_definitions(-DLDOM_USE_OWN_MEM_MAN=0)
add_definitions(-DCR3_ANTIWORD_PATCH=1 -DENABLE_ANTIWORD=1)
add_definitions(-DLVLONG_FILE_SUPPORT=1)
add_definitions(-DMAX_IMAGE_SCALE_MUL=2)
add_definitions(-DUSE_NANOSVG=1)
add_definitions(-DBUNDLED_FRIBIDI=1)
add_definitions(-DKO_LIBUNIBREAK_PATCH=1)       # libunibreak patched with "add_lb_get_char_class.patch" file from koreader
add_definitions(-DUSE_UTF8PROC=1)

#set(LOCAL_CFLAGS "-Wno-psabi -Wno-unused-variable -Wno-sign-compare -Wno-write-strings -Wno-main -Wno-unused-but-set-variable -Wno-unused-function -Wall")
# Option '-Wl,--no-merge-exidx-entries' removed as incompatible with clang.
set(LOCAL_CFLAGS "${LOCAL_CFLAGS} -funwind-tables")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${LOCAL_CFLAGS}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${LOCAL_CFLAGS}")

set(CMAKE_C_FLAGS_DEBUG             "-O0 -D_DEBUG -DDEBUG")
set(CMAKE_C_FLAGS_RELEASE           "-O3 -flto -DNDEBUG")
set(CMAKE_C_FLAGS_RELWITHDEBINFO    "-O3 -flto -DNDEBUG")
set(CMAKE_CXX_FLAGS_DEBUG           ${CMAKE_C_FLAGS_DEBUG})
set(CMAKE_CXX_FLAGS_RELEASE         ${CMAKE_C_FLAGS_RELEASE})
set(CMAKE_CXX_FLAGS_RELWITHDEBINFO  ${CMAKE_C_FLAGS_RELWITHDEBINFO})

set(CRENGINE_SRC_FILES
    ${CR3_ROOT}/crengine/src/cp_stats.cpp
    ${CR3_ROOT}/crengine/src/lvstring.cpp
    ${CR3_ROOT}/crengine/src/lvstring8collection.cpp
    ${CR3_ROOT}/crengine/src/lvstring32collection.cpp
    ${CR3_ROOT}/crengine/src/lvstring32hashedcollection.cpp
    ${CR3_ROOT}/crengine/src/crlog.cpp
    ${CR3_ROOT}/crengine/src/serialbuf.cpp
    ${CR3_ROOT}/crengine/src/props.cpp
    ${CR3_ROOT}/crengine/src/lstridmap.cpp
    ${CR3_ROOT}/crengine/src/rtfimp.cpp
    ${CR3_ROOT}/crengine/src/lvmemman.cpp
    ${CR3_ROOT}/crengine/src/lvstyles.cpp
    ${CR3_ROOT}/crengine/src/crtxtenc.cpp
    ${CR3_ROOT}/crengine/src/lvtinydom.cpp
    ${CR3_ROOT}/crengine/src/chmfmt.cpp
    ${CR3_ROOT}/crengine/src/epubfmt.cpp
    ${CR3_ROOT}/crengine/src/pdbfmt.cpp
    ${CR3_ROOT}/crengine/src/wordfmt.cpp
    ${CR3_ROOT}/crengine/src/lvopc.cpp
    ${CR3_ROOT}/crengine/src/docxfmt.cpp
    ${CR3_ROOT}/crengine/src/fb3fmt.cpp
    ${CR3_ROOT}/crengine/src/odtfmt.cpp
    ${CR3_ROOT}/crengine/src/odxutil.cpp
    ${CR3_ROOT}/crengine/src/lvstsheet.cpp
    ${CR3_ROOT}/crengine/src/crtest.cpp
    ${CR3_ROOT}/crengine/src/lvbmpbuf.cpp
    ${CR3_ROOT}/crengine/src/hyphman.cpp
    ${CR3_ROOT}/crengine/src/crskin.cpp
    ${CR3_ROOT}/crengine/src/lvdocview.cpp
    ${CR3_ROOT}/crengine/src/lvpagesplitter.cpp
    ${CR3_ROOT}/crengine/src/lvtextfm.cpp
    ${CR3_ROOT}/crengine/src/lvrend.cpp
    ${CR3_ROOT}/crengine/src/wolutil.cpp
    ${CR3_ROOT}/crengine/src/crconcurrent.cpp
    ${CR3_ROOT}/crengine/src/mathml.cpp
    ${CR3_ROOT}/crengine/src/hist.cpp
    ${CR3_ROOT}/crengine/src/xxhash.c
    ${CR3_ROOT}/crengine/src/textlang.cpp
    ${CR3_ROOT}/crengine/src/lvimg/lvcacheableobject.cpp
    ${CR3_ROOT}/crengine/src/lvimg/lvimagesource.cpp
    ${CR3_ROOT}/crengine/src/lvimg/crninepatchdecoder.cpp
    ${CR3_ROOT}/crengine/src/lvimg/lvjpegimagesource.cpp
    ${CR3_ROOT}/crengine/src/lvimg/lvpngimagesource.cpp
    ${CR3_ROOT}/crengine/src/lvimg/lvxpmimagesource.cpp
    ${CR3_ROOT}/crengine/src/lvimg/lvgifimagesource.cpp
    ${CR3_ROOT}/crengine/src/lvimg/lvgifframe.cpp
    ${CR3_ROOT}/crengine/src/lvimg/clzwdecoder.cpp
    ${CR3_ROOT}/crengine/src/lvimg/lvsvgimagesource.cpp
    ${CR3_ROOT}/crengine/src/lvimg/lvstretchimgsource.cpp
    ${CR3_ROOT}/crengine/src/lvimg/lvcolortransformimgsource.cpp
    ${CR3_ROOT}/crengine/src/lvimg/lvalphatransformimgsource.cpp
    ${CR3_ROOT}/crengine/src/lvimg/lvunpackedimgsource.cpp
    ${CR3_ROOT}/crengine/src/lvimg/lvdrawbufimgsource.cpp
    ${CR3_ROOT}/crengine/src/lvimg/lvimg.cpp
    ${CR3_ROOT}/crengine/src/lvfont/lvfnt.cpp
    ${CR3_ROOT}/crengine/src/lvfont/lvembeddedfont.cpp
    ${CR3_ROOT}/crengine/src/lvfont/lvfont.cpp
    ${CR3_ROOT}/crengine/src/lvfont/lvfntman.cpp
    ${CR3_ROOT}/crengine/src/lvfont/lvfontglyphcache.cpp
    ${CR3_ROOT}/crengine/src/lvfont/lvfontboldtransform.cpp
    ${CR3_ROOT}/crengine/src/lvfont/lvfontcache.cpp
    ${CR3_ROOT}/crengine/src/lvfont/lvfontdef.cpp
    ${CR3_ROOT}/crengine/src/lvfont/lvfreetypeface.cpp
    ${CR3_ROOT}/crengine/src/lvfont/lvfreetypefontman.cpp
    ${CR3_ROOT}/crengine/src/lvdrawbuf/lvbasedrawbuf.cpp
    ${CR3_ROOT}/crengine/src/lvdrawbuf/lvgraydrawbuf.cpp
    ${CR3_ROOT}/crengine/src/lvdrawbuf/lvcolordrawbuf.cpp
    ${CR3_ROOT}/crengine/src/lvdrawbuf/lvinkmeasurementdrawbuf.cpp
    ${CR3_ROOT}/crengine/src/lvdrawbuf/lvimagescaleddrawcallback.cpp
    ${CR3_ROOT}/crengine/src/lvdrawbuf/lvdrawbuf_utils.cpp
    ${CR3_ROOT}/crengine/src/lvstream/lvdefstreambuffer.cpp
    ${CR3_ROOT}/crengine/src/lvstream/lvfilemappedstream.cpp
    ${CR3_ROOT}/crengine/src/lvstream/lvnamedstream.cpp
    ${CR3_ROOT}/crengine/src/lvstream/lvfilestream.cpp
    ${CR3_ROOT}/crengine/src/lvstream/lvdirectorycontainer.cpp
    ${CR3_ROOT}/crengine/src/lvstream/lvcachedstream.cpp
    ${CR3_ROOT}/crengine/src/lvstream/lvzipdecodestream.cpp
    ${CR3_ROOT}/crengine/src/lvstream/lvziparc.cpp
    ${CR3_ROOT}/crengine/src/lvstream/lvrararc.cpp
    ${CR3_ROOT}/crengine/src/lvstream/lvmemorystream.cpp
    ${CR3_ROOT}/crengine/src/lvstream/lvtcrstream.cpp
    ${CR3_ROOT}/crengine/src/lvstream/lvblockwritestream.cpp
    ${CR3_ROOT}/crengine/src/lvstream/lvstream.cpp
    ${CR3_ROOT}/crengine/src/lvstream/lvbase64stream.cpp
    ${CR3_ROOT}/crengine/src/lvstream/lvstreamutils.cpp
    ${CR3_ROOT}/crengine/src/lvxml/lvfileparserbase.cpp
    ${CR3_ROOT}/crengine/src/lvxml/lvtextfilebase.cpp
    ${CR3_ROOT}/crengine/src/lvxml/lvtextparser.cpp
    ${CR3_ROOT}/crengine/src/lvxml/lvtextlinequeue.cpp
    ${CR3_ROOT}/crengine/src/lvxml/pmltextimport.cpp
    ${CR3_ROOT}/crengine/src/lvxml/lvtextrobustparser.cpp
    ${CR3_ROOT}/crengine/src/lvxml/lvtextbookmarkparser.cpp
    ${CR3_ROOT}/crengine/src/lvxml/lvxmlparser.cpp
    ${CR3_ROOT}/crengine/src/lvxml/lvhtmlparser.cpp
    ${CR3_ROOT}/crengine/src/lvxml/fb2coverpageparsercallback.cpp
    ${CR3_ROOT}/crengine/src/lvxml/lvxmlutils.cpp
    ${CR3_ROOT}/crengine/src/locale_data/fc-lang-data.c
    ${CR3_ROOT}/crengine/src/locale_data/crlocaledata.cpp
)
#    ${CR3_ROOT}/crengine/src/cri18n.cpp
#    ${CR3_ROOT}/crengine/src/crgui.cpp

set(COFFEECATCH_SRC_FILES
    ${CR3_ROOT}/android/jni/coffeecatch/coffeecatch.c
    ${CR3_ROOT}/android/jni/coffeecatch/coffeejni.c
)

set(JNI_SRC_FILES
    ${CR3_ROOT}/android/jni/cr3engine.cpp
    ${CR3_ROOT}/android/jni/cr3java.cpp
    ${CR3_ROOT}/android/jni/docview.cpp
)

set(LOCAL_SRC_FILES
    ${JNI_SRC_FILES}
    ${CRENGINE_SRC_FILES}
)

# Add coffeecatch only on supported architectures.
if("${ANDROID_ABI}" STREQUAL "armeabi-v7a" OR "${ANDROID_ABI}" STREQUAL "arm64-v8a" OR "${ANDROID_ABI}" STREQUAL "mips" OR "${ANDROID_ABI}" STREQUAL "x86")
    set(LOCAL_SRC_FILES ${LOCAL_SRC_FILES} ${COFFEECATCH_SRC_FILES})
endif("${ANDROID_ABI}" STREQUAL "armeabi-v7a" OR "${ANDROID_ABI}" STREQUAL "arm64-v8a" OR "${ANDROID_ABI}" STREQUAL "mips" OR "${ANDROID_ABI}" STREQUAL "x86")

# thirdparty libraries
add_subdirectory(thirdparty_libs)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             cr3engine-3-2-X

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             ${LOCAL_SRC_FILES} )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        cr3engine-3-2-X

        # thirdparty static libs
        local_png
        local_jpeg
        local_freetype
        local_harfbuzz
        local_chmlib
        local_antiword
        local_fribidi
        local_unibreak
        local_qimagescale
        local_zstd
        local_utf8proc

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        -lm -lz -ldl)
