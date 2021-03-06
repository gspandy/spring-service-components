/**
 * @author XiongJie, Date: 13-10-21
 */

package net.happyonroad.redis;

import redis.clients.jedis.Jedis;

/** Runnable in jedis support */
interface RedisRunnable {
    void run(Jedis jedis);
}
