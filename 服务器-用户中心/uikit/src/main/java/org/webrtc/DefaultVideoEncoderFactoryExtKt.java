package org.webrtc;

import android.media.MediaCodecInfo;

public class DefaultVideoEncoderFactoryExtKt {

    public static VideoEncoderFactory createVideoEncoderFactory(
            EglBase.Context eglContext,
            boolean enableIntelVp8Encoder,
            boolean enableH264HighProfile,
            Predicate<MediaCodecInfo> codecAllowedPredicate){
        return new DefaultVideoEncoderFactory(
                new HardwareVideoEncoderFactory(
                        eglContext,
                        enableIntelVp8Encoder,
                        enableH264HighProfile,
                        codecAllowedPredicate));
    }

    public static VideoEncoderFactory createCustomVideoEncoderFactory(
            EglBase.Context eglContext,
            boolean enableIntelVp8Encoder,
            boolean enableH264HighProfile,
            Predicate<MediaCodecInfo> codecAllowedPredicate,
            VideoEncoderSupportedCallback videoEncoderSupportedCallback){
        return new DefaultVideoEncoderFactory(
                new CustomHardwareVideoEncoderFactory(
                        eglContext,
                        enableIntelVp8Encoder,
                        enableH264HighProfile,
                        codecAllowedPredicate,
                        videoEncoderSupportedCallback));
    }

    public static VideoEncoderFactory createCustomVideoEncoderFactory(
            EglBase.Context eglContext,
            boolean enableIntelVp8Encoder,
            boolean enableH264HighProfile,
            VideoEncoderSupportedCallback videoEncoderSupportedCallback){
        return createCustomVideoEncoderFactory(
                eglContext,
                enableIntelVp8Encoder,
                enableH264HighProfile,
                null,
                videoEncoderSupportedCallback);
    }
}
