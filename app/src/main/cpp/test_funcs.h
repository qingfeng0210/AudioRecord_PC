//
// Created by qingf on 2017/2/20.
//

#ifndef AUDIORECORD_TEST_FUNCS_H
#define AUDIORECORD_TEST_FUNCS_H

#ifdef __cplusplus
extern "C" {
#endif
#include <android/log.h>

#define  LOG_TAG    "Mqtt"

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


#ifdef __cplusplus
}
#endif
#endif //AUDIORECORD_TEST_FUNCS_H
