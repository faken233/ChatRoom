package org.example.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWTUtil;
import org.example.exception.BusinessException;
import org.example.pojo.vo.ResultStatusEnum;

import java.time.LocalDateTime;

/**
 * @author yinjunbiao
 * @version 1.0
 * @date 2024/5/5
 */
public class JwtUtil {
    public static String parseJwt(String token) throws BusinessException {

        JSONObject claimsJson = null;
        try {
            claimsJson = JWTUtil.parseToken(token).getPayload().getClaimsJson();
        } catch (Exception e) {
            throw new BusinessException(ResultStatusEnum.TOKEN_EXPIRED);
        }
        Object exp = claimsJson.get("exp");
        LocalDateTime expireTime = TimeUtil.transfer(Long.parseLong(exp.toString()), LocalDateTime.class);
        if (expireTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ResultStatusEnum.TOKEN_EXPIRED);
        }

        return claimsJson.toString();
    }
}
