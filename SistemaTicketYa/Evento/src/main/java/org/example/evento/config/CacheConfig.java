package org.example.evento.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/* Cache en memoria de cada instancia (Caffeine). Ver CACHE_Y_BUSQUEDA.md para la justificacion.
 * Cada cache tiene su propio TTL; ademas se invalida al modificar los datos (@CacheEvict). */
@Configuration
@EnableCaching
public class CacheConfig {
    public static final String EVENTOS = "eventos";      // detalle de un evento
    public static final String BUSQUEDAS = "busquedas";  // resultados de busqueda
    public static final String LUGARES = "lugares";      // detalle de un lugar

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache(EVENTOS, Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(10)).maximumSize(1_000).build());
        manager.registerCustomCache(BUSQUEDAS, Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(2)).maximumSize(500).build());
        manager.registerCustomCache(LUGARES, Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(30)).maximumSize(500).build());
        return manager;
    }
}
