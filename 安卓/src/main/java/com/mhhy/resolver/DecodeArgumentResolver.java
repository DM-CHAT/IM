package com.mhhy.resolver;


import com.mhhy.annotation.Login;
import com.mhhy.util.CodecUtil;
import com.mhhy.util.JsonUtil;
import com.mhhy.util.StreamUtil;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class DecodeArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Login login = parameter.getMethod().getAnnotation(Login.class);
        return login != null && login.encrypted();
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Class<?>[] params = parameter.getMethod().getParameterTypes();
        if (params.length != 1) {
            throw new RuntimeException("后台接口参数异常");
        }
        Class<?> targetClass = params[0];
        String body = CodecUtil.decodeURL(StreamUtil.getString(((ServletWebRequest) webRequest).getRequest().getInputStream()));
        if (body == null || body.trim().length() == 0) {
            return null;
        }

        //todo aes解密
        return JsonUtil.fromJson(body, targetClass);
    }
}
