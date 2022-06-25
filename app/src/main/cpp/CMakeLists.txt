cmake_minimum_required(VERSION 3.18.1)

project("event_inj")

include(AndroidNdkGdb)
include(AndroidNdkModules)

set(LIBRARY_OUTPUT_PATH ${PROJECT_BINARY_DIR}/src/main/libs)

set(LIBRARY_DEPS android log)
android_ndk_gdb_enable()
#
## Searches for a specified prebuilt library and stores the path as a
## variable. Because CMake includes system libraries in the search path by
## default, you only need to specify the name of the public NDK library
## you want to add. CMake verifies that the library exists before
## completing its build.
#find_library( # Sets the name of the path variable.
#        log-lib
#
#        # Specifies the name of the NDK library that
#        # you want CMake to locate.
#        log)
#find_library(ANDROID_LOG_LIB log)

## Specifies libraries CMake should link to your target library. You
## can link multiple libraries, such as libraries you define in this
## build script, prebuilt third-party libraries, or system libraries.
#target_link_libraries( # Specifies the target library.
#        # Links the target library to the log library
#        # included in the NDK.
#        ${ANDROID_LOG_LIB}
#        ${log-lib})

add_executable(event_inj
        event_inj.cpp )

target_link_libraries(event_inj log android)
