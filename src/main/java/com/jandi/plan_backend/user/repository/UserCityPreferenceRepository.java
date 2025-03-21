package com.jandi.plan_backend.user.repository;

import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.entity.UserCityPreference;
import com.jandi.plan_backend.user.entity.UserCityPreferenceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCityPreferenceRepository extends JpaRepository<UserCityPreference, UserCityPreferenceId> {

    Optional<UserCityPreference> findByCity_CityIdAndUser_UserId(Integer CityId, Integer UserId);

    Optional<Object> findByCityAndUser(City curCity, User user);

    List<UserCityPreference> findByUser_UserId(Integer UserId);

    boolean existsByCity(City city);

    List<UserCityPreference> findByUser(User user);
}
