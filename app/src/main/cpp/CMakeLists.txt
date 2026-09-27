cmake_minimum_required(VERSION 3.22.1)
project("box2d_android")

# Support 16 KB memory page sizes
add_link_options("-Wl,-z,max-page-size=16384")


find_package(SDL3 REQUIRED CONFIG)

find_package(SDL3_image REQUIRED CONFIG)
find_package(SDL3_mixer REQUIRED CONFIG)
find_package(SDL3_ttf REQUIRED CONFIG)
# Disable Box2D 3 samples and unit tests
set(BOX2D_SAMPLES OFF CACHE BOOL "" FORCE)
set(BOX2D_UNIT_TESTS OFF CACHE BOOL "" FORCE)

# Add Box2D 3 source tree
add_subdirectory(box2d)

# Your JNI wrapper shared library
add_library(sdl_test SHARED main.c)

# Native physics shared library (Box2D wrapper)
add_library(native-physics SHARED native-lib.c)

target_include_directories(native-physics PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/box2d/include
)

# Link your targets with their required libraries
find_library(log-lib log)

target_link_libraries(sdl_test
        PRIVATE
        ${log-lib}
        SDL3::SDL3
        SDL3_image::SDL3_image
        SDL3_mixer::SDL3_mixer
        SDL3_ttf::SDL3_ttf
        android
)

target_link_libraries(native-physics
        PRIVATE
        ${log-lib}
        box2d
        android
)

