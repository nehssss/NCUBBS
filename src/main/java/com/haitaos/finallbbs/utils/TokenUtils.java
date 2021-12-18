package com.haitaos.finallbbs.utils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.haitaos.finallbbs.dto.ResultDTO;
import com.haitaos.finallbbs.dto.UserDTO;
import com.haitaos.finallbbs.exception.CustomizeErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;



@Component("TokenUtils")
public class TokenUtils {
    //private static final String SECRET = "cn.niter.sso";
    private static String SECRET ;
    @Value("${site.jwt.secret}")
    public  void setSecret(String SECRET) {
        this.SECRET= SECRET;
    }

    public String getToken(UserDTO user) {
        String token = "";
        token = JWT.create()
                .withIssuer("NCUUser")
                .withClaim("name", user.getName())
                .withClaim("id",user.getId())
                .withClaim("avatarUrl",user.getAvatarUrl())
                .withClaim("groupId",user.getGroupId())
                .withClaim("vipRank",user.getVipRank())
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000 * 24 * 3))//三天
                .sign(Algorithm.HMAC256(SECRET));// 以 password 作为 token 的密钥
        return token;
    }

    public static ResultDTO verifyToken(String token) throws Exception {

        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(SECRET)).withIssuer("NCUUser").build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            Map<String, Claim> map = verify.getClaims();

            UserDTO userDTO = new UserDTO();
            userDTO.setId(map.get("id").asLong());
            userDTO.setName(map.get("name").asString());
            userDTO.setAvatarUrl(map.get("avatarUrl").asString());
            userDTO.setVipRank(map.get("vipRank").asInt());
            userDTO.setGroupId(map.get("groupId").asInt());
            Map<String, String> resultMap = new HashMap<>(map.size());
            map.forEach((k, v) -> resultMap.put(k, v.asString()));

            return ResultDTO.okOf(userDTO);

        } catch (JWTVerificationException e) {

            return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN);
        }


    }

}
