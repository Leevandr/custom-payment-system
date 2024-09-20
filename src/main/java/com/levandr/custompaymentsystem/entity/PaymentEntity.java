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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "ref_payment")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "payment_id")
    String paymentId;

    @Column(name = "record_number")
    String recordNumber;

    @Column(name = "company_name")
    String companyName;

    @Column(name = "payer_inn")
    String payerInn;

    @Column(name = "amount")
    BigDecimal amount;

    @Column(name = "status_code")
    Integer status;

    public PaymentEntity(String recordNumber, String paymentId, String companyName, String payerInn, BigDecimal amount) {
        this.recordNumber = recordNumber;
        this.paymentId = paymentId;
        this.companyName = companyName;
        this.payerInn = payerInn;
        this.amount = amount;
    }

    public PaymentEntity() {

    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ?
                ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        PaymentEntity that = (PaymentEntity) o;
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
               ", status=" + status +
               '}';
    }
}
