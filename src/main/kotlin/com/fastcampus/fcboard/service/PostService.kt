package com.fastcampus.fcboard.service

import com.fastcampus.fcboard.exception.PostNotDeletableException
import com.fastcampus.fcboard.exception.PostNotFoundException
import com.fastcampus.fcboard.repository.PostRepository
import com.fastcampus.fcboard.service.dto.PostCreateRequestDto
import com.fastcampus.fcboard.service.dto.PostUpdateRequestDto
import com.fastcampus.fcboard.service.dto.toEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PostService(
    private val postRepository: PostRepository,
) {
    // 위에 readonly라 되어 있기 때문에, 한번더 지정해주면서 readonly를 빼주어야 합니다.
    // 함수단위에 있는게 먼저 적용됩니다
    @Transactional
    fun createPost(requestDto: PostCreateRequestDto) :Long {
        return postRepository.save(requestDto.toEntity()).id
    }

    @Transactional
    fun updatePost(id: Long, requestDto: PostUpdateRequestDto):Long {
        println(id)
        val post = postRepository.findByIdOrNull(id) ?: throw PostNotFoundException()
        post.update(requestDto)
        return id
    }

    @Transactional
    fun deletePost(id: Long, deletedBy: String): Long {
        val post = postRepository.findByIdOrNull(id) ?: throw PostNotFoundException()
        if(post.createdBy != deletedBy) throw PostNotDeletableException()
        postRepository.delete(post)
        return id
    }
}
