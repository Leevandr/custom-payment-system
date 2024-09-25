package com.levandr.custompaymentsystem.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id;

    @Column(name = "payment_id", nullable = false)
    String paymentId;

    @Column(name = "record_number", nullable = false)
    String recordNumber;

    @Column(name = "company_name", nullable = false)
    String companyName;

    @Column(name = "payer_inn", nullable = false)
    String payerInn;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    BigDecimal amount;

    @Column(name = "status_code", nullable = false)
    Integer statusCode;

    @Column(name = "file_name", nullable = false)
    String fileName;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ?
                ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Payment that = (Payment) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "PaymentEntity{" +
               "id=" + id +
               ", paymentId='" + paymentId + '\'' +
               ", recordNumber='" + recordNumber + '\'' +
               ", companyName='" + companyName + '\'' +
               ", payerInn='" + payerInn + '\'' +
               ", amount=" + amount +
               ", status=" + statusCode +
               '}';
    }
}
