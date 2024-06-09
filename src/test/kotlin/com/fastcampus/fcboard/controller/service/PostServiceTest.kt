package com.fastcampus.fcboard.controller.service

import com.fastcampus.fcboard.domain.Post
import com.fastcampus.fcboard.exception.PostNotDeletableException
import com.fastcampus.fcboard.exception.PostNotFoundException
import com.fastcampus.fcboard.exception.PostNotUpdatableException
import com.fastcampus.fcboard.service.PostService
import com.fastcampus.fcboard.service.dto.PostCreateRequestDto
import com.fastcampus.fcboard.repository.PostRepository
import com.fastcampus.fcboard.service.dto.PostUpdateRequestDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.persistence.PostUpdate
import jakarta.validation.constraints.Null
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
class PostServiceTest(
    private val postService : PostService,
    private val postRepository: PostRepository,
) : BehaviorSpec({
    given("게시물 생성시"){
        When("게시물 생성"){
            val postId = postService.createPost(PostCreateRequestDto(
                title = "제목",
                content = "내용",
                createdBy = "harris",
            ))
            then("게시물이 정상적으로 생성됨을 확인한다."){
                postId shouldBeGreaterThan 0L
                val post = postRepository.findByIdOrNull(postId)
                post shouldNotBe null
                post?.title shouldBe "제목"
                post?.content shouldBe "내용"
                post?.createdBy shouldBe "harris"
            }
        }
    }

    given("게시물 수정시"){
        val saved = postRepository.save(Post(title="title", content = "content", createdBy = "harris"))
        When("정상 수정시"){
            val updatedId = postService.updatePost(saved.id, PostUpdateRequestDto(
                title = "updated title",
                content = "updated content",
                updatedBy = "harris",
            ))
            then("게시물이 정상적으로 수정됨을 확인"){
                saved.id shouldBe updatedId
                val updated = postRepository.findByIdOrNull(updatedId)
                updated shouldNotBe null
                updated?.title shouldBe "updated title"
                updated?.content shouldBe "updated content"
//                updated?.createdBy shouldBe "harris"
            }
        }
        When("게시물이 없을 때"){
            then("게시물을 찾을 수 없다는 예외 발생"){
                shouldThrow<PostNotFoundException> {
                    postService.updatePost(9999L,PostUpdateRequestDto(
                        title = "updated title",
                        content = "updated content",
                        updatedBy = "update harris",
                    ))
                }
            }
        }
        When("작성자가 동일하지 않을때"){
            then("수정할 수 없는 게시물이다 예외 발생"){
                shouldThrow<PostNotUpdatableException> {
                    postService.updatePost(1L,PostUpdateRequestDto(
                        title = "updated title",
                        content = "updated content",
                        updatedBy = "not harris",
                    ))
                }
            }
        }
    }
    given("게시물 삭제시"){
        val saved = postRepository.save(Post(title="title", content = "content", createdBy = "harris"))
        When("정상 삭제시"){
            val postId = postService.deletePost(saved.id, "harris")
            then("게시물이 정상적으로 삭제됨을 확인"){
                postId shouldBe saved.id
                postRepository.findByIdOrNull(postId) shouldBe null
            }
        }
        When("작성자가 동일하지 않으면"){
            val saved2 = postRepository.save(Post(title="title", content = "content", createdBy = "harris"))
            then("삭제할 수 없는 게시물이다 예외 발생!"){
                shouldThrow<PostNotDeletableException> {
                    postService.deletePost(saved2.id, "not harris")
                }
            }
        }
    }
})
