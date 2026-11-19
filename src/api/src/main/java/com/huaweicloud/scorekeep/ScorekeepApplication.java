package com.huaweicloud.scorekeep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 对局记录服务入口。
 *
 * <p>服务同时提供两类能力：</p>
 * <ul>
 *   <li>{@code /api/**} —— 对局、场次、用户、落子、局面的 REST 接口；</li>
 *   <li>{@code /} —— 静态页面，由同一进程的静态资源映射提供。</li>
 * </ul>
 */
@SpringBootApplication
public class ScorekeepApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScorekeepApplication.class, args);
    }
}
