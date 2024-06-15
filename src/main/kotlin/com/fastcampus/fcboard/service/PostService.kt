package com.fastcampus.fcboard.service

import com.fastcampus.fcboard.exception.PostNotDeletableException
import com.fastcampus.fcboard.exception.PostNotFoundException
import com.fastcampus.fcboard.repository.PostRepository
import com.fastcampus.fcboard.service.dto.PostCreateRequestDto
import com.fastcampus.fcboard.service.dto.PostDetailResponseDto
import com.fastcampus.fcboard.service.dto.PostSearchRequestDto
import com.fastcampus.fcboard.service.dto.PostSummaryResponseDto
import com.fastcampus.fcboard.service.dto.PostUpdateRequestDto
import com.fastcampus.fcboard.service.dto.toDetailResponseDto
import com.fastcampus.fcboard.service.dto.toEntity
import com.fastcampus.fcboard.service.dto.toSummaryResponseDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
    fun createPost(requestDto: PostCreateRequestDto): Long {
        return postRepository.save(requestDto.toEntity()).id
    }

    @Transactional
    fun updatePost(id: Long, requestDto: PostUpdateRequestDto): Long {
        println(id)
        val post = postRepository.findByIdOrNull(id) ?: throw PostNotFoundException()
        post.update(requestDto)
        return id
    }

    @Transactional
    fun deletePost(id: Long, deletedBy: String): Long {
        val post = postRepository.findByIdOrNull(id) ?: throw PostNotFoundException()
        if (post.createdBy != deletedBy) throw PostNotDeletableException()
        postRepository.delete(post)
        return id
    }

    fun getPost(id: Long): PostDetailResponseDto {
        return postRepository.findByIdOrNull(id)?.toDetailResponseDto() ?: throw PostNotFoundException()
    }

    // Paging / Pagination
    // DB에 저장된 데이터들을 페이지에 맞춰서 몇 개씩 뿌릴건지 알려주는 것
    // JPA - Pageable
    // 이를 위해서 JPA에서 Pageable이라는 객체를 제공하고있다.
    // Pageable이란?Pageable 이 Pagination 요청 정보를 담기위한 추상 인터페이스 라는 의미/실제로 쓰기 위해서
    // ervice 로직 내에서 가공된 List를 Paging 하고 싶다면?
    // --> PageImpl을 사용하자
    // Spring Data JPA에서 Page 인터페이스의 구현 클래스 중 하나이다.
    // 생성자를 통해 List 객체, Pageable 객체, 전체 데이터의 총 개수를 받아 Page 객체를 생성한다.
    fun findPageBy(pageRequest: Pageable, postSearchRequestDto: PostSearchRequestDto): Page<PostSummaryResponseDto> {
//        return postRepository.findAll(pageRequest).toSummaryResponseDto()
        return postRepository.findPageBy(pageRequest, postSearchRequestDto).toSummaryResponseDto()
    }
}
