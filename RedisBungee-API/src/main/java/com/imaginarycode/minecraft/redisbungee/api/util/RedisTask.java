package com.imaginarycode.minecraft.redisbungee.api.util;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.imaginarycode.minecraft.redisbungee.api.RedisBungeePlugin;
import com.imaginarycode.minecraft.redisbungee.api.summoners.ClusterJedisSummoner;
import com.imaginarycode.minecraft.redisbungee.api.summoners.JedisSummoner;
import com.imaginarycode.minecraft.redisbungee.api.summoners.Summoner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.concurrent.Callable;

public abstract class RedisTask<V> implements Runnable, Callable<V> {

    private final Summoner<?> summoner;
    private final RedisBungeeAPI api;

    private RedisBungeePlugin<?> plugin;

    @Override
    public V call() throws Exception {
        return execute();
    }

    public RedisTask(RedisBungeeAPI api) {
        this.api = api;
        this.summoner = api.getSummoner();
    }

    public RedisTask(RedisBungeePlugin<?> plugin) {
        this.plugin = plugin;
        this.api = plugin.getApi();
        this.summoner = api.getSummoner();
    }

    public abstract V singleJedisTask(Jedis jedis);

    public abstract V clusterJedisTask(JedisCluster jedisCluster);

    @Override
    public void run() {
        this.execute();
    }

    public V execute(){
        if (api.getMode() == RedisBungeeMode.SINGLE) {
            JedisSummoner jedisSummoner = (JedisSummoner) summoner;
            try (Jedis jedis = jedisSummoner.obtainResource()) {
                return this.singleJedisTask(jedis);
            }

        } else if (api.getMode() == RedisBungeeMode.CLUSTER) {
            ClusterJedisSummoner clusterJedisSummoner = (ClusterJedisSummoner) summoner;
            return this.clusterJedisTask(clusterJedisSummoner.obtainResource());
        }
        return null;
    }

    public RedisBungeePlugin<?> getPlugin() {
        if (plugin == null) {
            throw new NullPointerException("Plugin is null in the task");
        }
        return plugin;
    }
}