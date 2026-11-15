package com.jinanging.restareaapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.jinanging.restareaapi.domain.MenuItem;
import com.jinanging.restareaapi.domain.RestStop;
import com.jinanging.restareaapi.repository.RestStopRepository;

@Service
public class RestStopService {
    private final RestStopRepository repository;
    private final SouthboundRoute route = new SouthboundRoute();

    public RestStopService(RestStopRepository repository) {
        this.repository = repository;
    }

    public List<RestStop> list() {
        return repository.findAll();
    }

    public List<RestStop> next(double latitude, double longitude) {
        return route.next(latitude, longitude, repository.findAll());
    }

    public List<MenuItem> menus(String id) {
        return repository.findMenusByRestStopId(id);
    }

    public Optional<RestStop> findById(String id) {
        return repository.findById(id);
    }
}
