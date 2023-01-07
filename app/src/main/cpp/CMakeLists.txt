# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.18.1)

project(native-lib C)

add_library(native-lib SHARED native-lib.cpp)
find_library(log-lib log)
add_library(toybox SHARED IMPORTED)

set_target_properties(native-lib PROPERTIES LINKER_LANGUAGE C)
set_target_properties(toybox PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libtoybox.so)

include_directories(/home/atomofiron/StudioProjects/android-search-box-app/app/src/main/cpp/toybox_headers/)

target_link_libraries(
        native-lib
        ${log-lib}
        toybox
)


























