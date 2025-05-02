package com.nxmbit.wxcompare.repository;

import com.nxmbit.wxcompare.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.nxmbit.wxcompare.util.SqliteDbUtil;

import java.util.Optional;

public class UserRepository {

    public Optional<User> findFirstUser() {
        try (Session session = SqliteDbUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User", User.class)
                    .setMaxResults(1)
                    .uniqueResultOptional();
        }
    }

    public User save(User user) {
        Transaction transaction = null;
        try (Session session = SqliteDbUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            if (user.getId() == null) {
                session.persist(user);
            } else {
                user = session.merge(user);
            }

            transaction.commit();
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }
}