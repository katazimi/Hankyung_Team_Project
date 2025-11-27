package com.hk.chart.repository;

import com.hk.chart.entity.User;
import com.hk.chart.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    List<Watchlist> findAllByUser(User user);
    Optional<Watchlist> findByUserAndStockCode(User user, String stockCode);
    boolean existsByUserAndStockCode(User user, String stockCode);
    void deleteByUserAndStockCode(User user, String stockCode);
}