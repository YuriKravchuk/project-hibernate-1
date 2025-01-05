package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {

    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "11111111");
        properties.put(Environment.HBM2DDL_AUTO, "update");
        sessionFactory = new Configuration()
                .addAnnotatedClass(Player.class)
                .setProperties(properties)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try(Session session = sessionFactory.openSession()) {
            String sql = "SELECT * FROM player";
            Query<Player> query = session.createNativeQuery(sql, Player.class);
//            List<Player> players = query.getResultList();
//            int startIndex = (pageNumber) * pageSize;
//            int endIndex = startIndex + pageSize;
//            players = players.subList(startIndex, endIndex);
//            return players;
            query.setFirstResult((pageNumber) * pageSize);
            query.setMaxResults(pageSize);
            return query.list();
        }
    }

    @Override
    public int getAllCount() {
        try(Session session = sessionFactory.openSession()) {
            Query <Long> query = session.createNamedQuery("player.getAllCount", Long.class);
            return Math.toIntExact(query.getSingleResult());
        }
    }

    @Override
    public Player save(Player player) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.saveOrUpdate(player);
            transaction.commit();
            return player;
        }
    }

    @Override
    public Player update(Player player) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(player);
            transaction.commit();
            return player;
        }
    }

    @Override
    public Optional<Player> findById(long id) {
        try(Session session = sessionFactory.openSession()) {
            String hql = "from Player WHERE id = :id";
            Query<Player> query = session.createQuery(hql, Player.class)
                    .setParameter("id", id);
            Player player = query.getSingleResult();
            return Optional.ofNullable(player);

        }
    }

    @Override
    public void delete(Player player) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.remove(player);
            session.flush();
            transaction.commit();
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}