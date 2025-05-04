package com.nxmbit.wxcompare.repository;

import com.nxmbit.wxcompare.model.Location;
import com.nxmbit.wxcompare.util.SqliteDbUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class LocationRepository {

    public Location save(Location location) {
        try (Session session = SqliteDbUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.persist(location);
                transaction.commit();
                return location;
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public Optional<Location> findByCoordinates(Double latitude, Double longitude) {
        try (Session session = SqliteDbUtil.getSessionFactory().openSession()) {
            Query<Location> query = session.createQuery(
                    "from Location where latitude = :latitude and longitude = :longitude",
                    Location.class);
            query.setParameter("latitude", latitude);
            query.setParameter("longitude", longitude);
            return query.uniqueResultOptional();
        }
    }

    public List<Location> findAll() {
        try (Session session = SqliteDbUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Location", Location.class).list();
        }
    }

    public void delete(Long id) {
        try (Session session = SqliteDbUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Location location = session.get(Location.class, id);
                if (location != null) {
                    session.remove(location);
                }
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }
}