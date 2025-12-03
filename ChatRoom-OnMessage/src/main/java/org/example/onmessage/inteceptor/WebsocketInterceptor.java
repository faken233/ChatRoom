package org.example.onmessage.inteceptor;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.net.url.UrlQuery;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.constant.GlobalConstants;
import org.example.pojo.bo.UserBO;
import org.example.utils.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author yinjunbiao
 * @version 1.0
 * @date 2024/5/4
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebsocketInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
        //1. 获取以base64加密的jsontoken
        UrlQuery query = UrlQuery.of(serverHttpRequest.getURI().getQuery(), StandardCharsets.UTF_8);
        String token = query.get("token").toString();
        if (!StringUtils.hasText(token)) {
            throw new RuntimeException("服务连接失败, 请登录");
        }

        //2.1 解析token
        String json = JwtUtil.parseJwt(token);
        JSONObject userJson = JSON.parseObject(json);

        //2.2 获取jsonToken中的用户角色
        String user = userJson.get("principal").toString();

        //2.3 权限信息
        JSONArray authoritiesArray = userJson.getJSONArray("authorities");

        //2.4 转为数组
        String[] authorities = null;

        try {
            authorities = authoritiesArray.toArray(new String[0]);
        } catch (Exception e) {
            log.warn("权限信息解析失败", e);
        }

        UserBO userObj = null;

        //2.5 解析为用户对象
        if (StringUtils.hasText(user)) {
            userObj = JSONObject.parseObject(user, UserBO.class);
            map.put("user", userObj);
        }

        List<String> strings = null;

        try {
            strings = new ArrayList<>(Arrays.asList(authorities));
        } catch (Exception e) {
            log.warn("权限信息转换失败", e);
        }
        map.put("authorities", strings);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
    }
}
