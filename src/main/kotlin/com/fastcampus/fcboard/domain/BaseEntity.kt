package com.fastcampus.fcboard.domain

import jakarta.persistence.MappedSuperclass
import java.time.LocalDateTime

// BaseEntity를 상속하는 클래스들이 아래 필드들을 사용
// 단순히 매핑 정보를 상속할 목적으로만 사용된다.
// 이 클래스를 직접 생성해서 사용할 일은 거의 없으므로 추상 클래스로 만드는 것을 권장
@MappedSuperclass
abstract class BaseEntity(
    createdBy: String,
) {
    val createdBy: String = createdBy
    val createdAt: LocalDateTime? = LocalDateTime.now()
    var updatedBy: String = createdBy
        protected set
    var updatedAt: LocalDateTime? = null
        protected set

    fun update(updatedBy: String) {
        this.updatedBy = updatedBy
        this.updatedAt = LocalDateTime.now()
    }
}
