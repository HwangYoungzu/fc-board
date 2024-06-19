package com.fastcampus.fcboard.domain

import com.fastcampus.fcboard.exception.PostNotUpdatableException
import com.fastcampus.fcboard.service.dto.PostUpdateRequestDto
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
class Post(
    createdBy: String,
    title: String,
    content: String,
    tags: List<String> = emptyList(),
) : BaseEntity(
    createdBy
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    var title: String = title
        private set
    var content: String = content
        private set

    // 고아객체는 부모 엔티티와 연관관계가 끊어진 자식 엔티티를 가리킵니다.
    // 부모가 제거될때, 부모와 연관되어있는 모든 자식 엔티티들은 고아객체가 됩니다.
    // 부모 엔티티와 자식 엔티티 사이의 연관관계를 삭제할때, 해당 자식 엔티티는 고아객체가 됩니다.
    // orphanRemoval = true 로 하면, 이 고아객체들을 자동으로 삭제 처리해줍니다
    @OneToMany(mappedBy = "post", orphanRemoval = true, cascade = [CascadeType.ALL])
    var comments: MutableList<Comment> = mutableListOf()
        protected set

    @OneToMany(mappedBy = "post", orphanRemoval = true, cascade = [CascadeType.ALL])
    var tags: MutableList<Tag> = tags.map { Tag(it, this, createdBy) }.toMutableList()
        protected set

    fun update(postUpdateRequestDto: PostUpdateRequestDto) {
        if (postUpdateRequestDto.updatedBy != this.createdBy) {
            throw PostNotUpdatableException()
        }
        this.title = postUpdateRequestDto.title
        this.content = postUpdateRequestDto.content
        super.update(postUpdateRequestDto.updatedBy)
    }
}
