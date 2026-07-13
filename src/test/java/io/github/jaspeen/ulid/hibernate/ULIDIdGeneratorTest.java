package io.github.jaspeen.ulid.hibernate;

import io.github.jaspeen.ulid.ULID;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JavaType;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ULIDIdGeneratorTest {

    private static SessionFactory sessionFactory;

    @BeforeAll
    static void setUp() {
        sessionFactory = new Configuration()
                .addAnnotatedClass(ULIDEntity.class)
                .addAnnotatedClass(UUIDEntity.class)
                .addAnnotatedClass(StringEntity.class)
                .addAnnotatedClass(BytesEntity.class)
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:ulid_test;DB_CLOSE_DELAY=-1")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .buildSessionFactory();
    }

    @AfterAll
    static void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Entity(name = "ULIDEntity")
    static class ULIDEntity {
        @Id
        @GeneratedValue(generator = "ulid")
        @GenericGenerator(name = "ulid", strategy = "io.github.jaspeen.ulid.hibernate.ULIDIdGenerator")
        @JavaType(ULIDTypeDescriptor.class)
        ULID id;
    }

    @Entity(name = "UUIDEntity")
    static class UUIDEntity {
        @Id
        @GeneratedValue(generator = "ulid")
        @GenericGenerator(name = "ulid", strategy = "io.github.jaspeen.ulid.hibernate.ULIDIdGenerator")
        UUID id;
    }

    @Entity(name = "StringEntity")
    static class StringEntity {
        @Id
        @GeneratedValue(generator = "ulid")
        @GenericGenerator(name = "ulid", strategy = "io.github.jaspeen.ulid.hibernate.ULIDIdGenerator")
        String id;
    }

    @Entity(name = "BytesEntity")
    static class BytesEntity {
        @Id
        @GeneratedValue(generator = "ulid")
        @GenericGenerator(name = "ulid", strategy = "io.github.jaspeen.ulid.hibernate.ULIDIdGenerator")
        byte[] id;
    }

    @Test
    void generatesULIDId() {
        ULIDEntity entity = new ULIDEntity();
        sessionFactory.inTransaction(session -> session.persist(entity));
        assertNotNull(entity.id);
    }

    @Test
    void generatesUUIDId() {
        UUIDEntity entity = new UUIDEntity();
        sessionFactory.inTransaction(session -> session.persist(entity));
        assertNotNull(entity.id);
        assertEquals(7, entity.id.version());
    }

    @Test
    void generatesStringId() {
        StringEntity entity = new StringEntity();
        sessionFactory.inTransaction(session -> session.persist(entity));
        assertNotNull(entity.id);
        assertEquals(26, entity.id.length());
    }

    @Test
    void generatesBytesId() {
        BytesEntity entity = new BytesEntity();
        sessionFactory.inTransaction(session -> session.persist(entity));
        assertNotNull(entity.id);
        assertEquals(16, entity.id.length);
    }

    @Test
    void returnsExistingIdWhenEntityAlreadyHasOne() {
        UUIDEntity entity = new UUIDEntity();
        sessionFactory.inTransaction(session -> session.persist(entity));
        UUID existing = entity.id;

        ULIDIdGenerator generator = new ULIDIdGenerator();
        Object generated = sessionFactory.fromTransaction(session ->
                generator.generate((SharedSessionContractImplementor) session, entity));
        assertEquals(existing, generated);
    }
}
