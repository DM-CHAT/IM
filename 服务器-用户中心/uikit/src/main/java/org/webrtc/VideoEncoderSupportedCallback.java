package org.webrtc;

import android.media.MediaCodecInfo;

import androidx.annotation.NonNull;

public interface VideoEncoderSupportedCallback {
    /**
     * 注意当前{@link android.os.Build.VERSION#SDK_INT} 是否支持
     * {@link CustomHardwareVideoEncoderFactory#isHardwareSupportedInCurrentSdkVp8(MediaCodecInfo)}
     *
     * @param info 编码器信息
     * @return 是否支持VP8
     */
    default boolean isSupportedVp8(@NonNull MediaCodecInfo info) {
        return false;
    }

    /**
     * 注意当前{@link android.os.Build.VERSION#SDK_INT} 是否支持
     * {@link CustomHardwareVideoEncoderFactory#isHardwareSupportedInCurrentSdkVp9(MediaCodecInfo)}
     *
     * @param info 编码器信息
     * @return 是否支持VP9
     */
    default boolean isSupportedVp9(@NonNull MediaCodecInfo info) {
        return false;
    }

    /**
     * 注意当前{@link android.os.Build.VERSION#SDK_INT} 是否支持
     * {@link CustomHardwareVideoEncoderFactory#isHardwareSupportedInCurrentSdkH264(MediaCodecInfo)}
     *
     * @param info 编码器信息
     * @return 是否支持H264
     */
    boolean isSupportedH264(@NonNull MediaCodecInfo info);
}
