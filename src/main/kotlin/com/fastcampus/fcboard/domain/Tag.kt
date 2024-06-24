package com.fastcampus.fcboard.domain

import jakarta.persistence.*

@Entity
class Tag(
    name: String,
    post: Post,
    createdBy: String,
) : BaseEntity(createdBy) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    var name: String = name
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    // 외래키 제약 조건을 제거하기 위해 NO_CONSTRAINT 설정
    @JoinColumn(foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    var post: Post = post
        protected set
}
