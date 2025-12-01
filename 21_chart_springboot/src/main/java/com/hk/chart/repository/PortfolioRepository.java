package com.hk.chart.repository;

import com.hk.chart.entity.Portfolio;
import com.hk.chart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findAllByUser(User user);
}