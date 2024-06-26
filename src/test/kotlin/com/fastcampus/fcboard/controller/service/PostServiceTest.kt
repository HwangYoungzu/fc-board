package com.fastcampus.fcboard.controller.service

import com.fastcampus.fcboard.domain.Comment
import com.fastcampus.fcboard.domain.Post
import com.fastcampus.fcboard.domain.Tag
import com.fastcampus.fcboard.exception.PostNotDeletableException
import com.fastcampus.fcboard.exception.PostNotFoundException
import com.fastcampus.fcboard.exception.PostNotUpdatableException
import com.fastcampus.fcboard.repository.CommentRepository
import com.fastcampus.fcboard.repository.PostRepository
import com.fastcampus.fcboard.repository.TagRepository
import com.fastcampus.fcboard.service.LikeService
import com.fastcampus.fcboard.service.PostService
import com.fastcampus.fcboard.service.dto.PostCreateRequestDto
import com.fastcampus.fcboard.service.dto.PostSearchRequestDto
import com.fastcampus.fcboard.service.dto.PostUpdateRequestDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
class PostServiceTest(
    private val postService: PostService,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val tagRepository: TagRepository,
    private val likeService: LikeService,
) : BehaviorSpec({
    beforeSpec {
        postRepository.saveAll(
            listOf(
                Post(title = "title1", content = "content1", createdBy = "harris1", tags = listOf("tags1", "tags2")),
                Post(title = "title12", content = "content2", createdBy = "harris1", tags = listOf("tags1", "tags2")),
                Post(title = "title13", content = "content3", createdBy = "harris1", tags = listOf("tags1", "tags2")),
                Post(title = "title14", content = "content4", createdBy = "harris1", tags = listOf("tags1", "tags2")),
                Post(title = "title15", content = "content5", createdBy = "harris1", tags = listOf("tags1", "tags2")),
                Post(title = "title6", content = "content6", createdBy = "harris2", tags = listOf("tags1", "tags5")),
                Post(title = "title7", content = "content7", createdBy = "harris2", tags = listOf("tags1", "tags5")),
                Post(title = "title8", content = "content8", createdBy = "harris2", tags = listOf("tags1", "tags5")),
                Post(title = "title9", content = "content9", createdBy = "harris2", tags = listOf("tags1", "tags5")),
                Post(title = "title10", content = "content10", createdBy = "harris2", tags = listOf("tags1", "tags5"))
            )
        )
    }
    given("게시물 생성시") {
        When("게시물 인풋이 정상적으로 들어오면") {
            val postId = postService.createPost(
                PostCreateRequestDto(
                    title = "제목",
                    content = "내용",
                    createdBy = "harris"
                )
            )
            then("게시물이 정상적으로 생성됨을 확인한다.") {
                postId shouldBeGreaterThan 0L
                val post = postRepository.findByIdOrNull(postId)
                post shouldNotBe null
                post?.title shouldBe "제목"
                post?.content shouldBe "내용"
                post?.createdBy shouldBe "harris"
            }
        }
        When("태그가 추가되면") {
            val postId = postService.createPost(
                PostCreateRequestDto(
                    title = "제목",
                    content = "내용",
                    createdBy = "harris",
                    tags = listOf("tags1", "tags2")
                )
            )
            then("태그가 정상적으로 추가됨을 확인한다") {
                val tags = tagRepository.findByPostId(postId)
                tags.size shouldBe 2
                tags[0].name shouldBe "tags1"
                tags[1].name shouldBe "tags2"
            }
        }
    }

    given("게시물 수정시") {
        val saved = postRepository.save(
            Post(title = "title", content = "content", createdBy = "harris", tags = listOf("tag1", "tag2"))
        )
        When("정상 수정시") {
            val updatedId = postService.updatePost(
                saved.id,
                PostUpdateRequestDto(
                    title = "updated title",
                    content = "updated content",
                    updatedBy = "harris"
                )
            )
            then("게시물이 정상적으로 수정됨을 확인") {
                saved.id shouldBe updatedId
                val updated = postRepository.findByIdOrNull(updatedId)
                updated shouldNotBe null
                updated?.title shouldBe "updated title"
                updated?.content shouldBe "updated content"
//                updated?.createdBy shouldBe "harris"
            }
        }
        When("게시물이 없을 때") {
            then("게시물을 찾을 수 없다는 예외 발생") {
                shouldThrow<PostNotFoundException> {
                    postService.updatePost(
                        9999L,
                        PostUpdateRequestDto(
                            title = "updated title",
                            content = "updated content",
                            updatedBy = "update harris"
                        )
                    )
                }
            }
        }
        When("작성자가 동일하지 않을때") {
            then("수정할 수 없는 게시물이다 예외 발생") {
                shouldThrow<PostNotUpdatableException> {
                    postService.updatePost(
                        1L,
                        PostUpdateRequestDto(
                            title = "updated title",
                            content = "updated content",
                            updatedBy = "not harris"
                        )
                    )
                }
            }
        }
        When("태그가 수정되었을 때") {
            val updatedId = postService.updatePost(
                saved.id,
                PostUpdateRequestDto(
                    title = "updated title",
                    content = "updated content",
                    updatedBy = "harris",
                    tags = listOf("tags1", "tags2", "tags3")
                )
            )
            then("정상적으로 수정됨을 확인") {
                val tags = tagRepository.findByPostId(updatedId)
                tags.size shouldBe 3
                tags[2].name shouldBe "tags3"
            }
            then("태그 순서가 변경되었을 때 정상적으로 변경됨을 확인") {
                postService.updatePost(
                    saved.id,
                    PostUpdateRequestDto(
                        title = "updated title",
                        content = "updated content",
                        updatedBy = "harris",
                        tags = listOf("tags3", "tags2", "tags1")
                    )
                )
                val tags = tagRepository.findByPostId(updatedId)
                tags.size shouldBe 3
                tags[2].name shouldBe "tags1"
            }
        }
    }
    given("게시물 삭제시") {
        val saved = postRepository.save(Post(title = "title", content = "content", createdBy = "harris"))
        When("정상 삭제시") {
            val postId = postService.deletePost(saved.id, "harris")
            then("게시물이 정상적으로 삭제됨을 확인") {
                postId shouldBe saved.id
                postRepository.findByIdOrNull(postId) shouldBe null
            }
        }
        When("작성자가 동일하지 않으면") {
            val saved2 = postRepository.save(Post(title = "title", content = "content", createdBy = "harris"))
            then("삭제할 수 없는 게시물이다 예외 발생!") {
                shouldThrow<PostNotDeletableException> {
                    postService.deletePost(saved2.id, "not harris")
                }
            }
        }
    }
    given("게시뭃 상세조회시") {
        val saved = postRepository.save(Post(title = "title", content = "content", createdBy = "harris"))
        tagRepository.saveAll(
            listOf(
                Tag(name = "tag1", post = saved, createdBy = "harris"),
                Tag(name = "tag2", post = saved, createdBy = "harris"),
                Tag(name = "tag3", post = saved, createdBy = "harris")
            )
        )
        likeService.createLike(saved.id, "harris")
        likeService.createLike(saved.id, "harris1")
        likeService.createLike(saved.id, "harris2")
        When("정상 조회시") {
            val post = postService.getPost(saved.id)
            then("게시물의 내용이 정상적으로 변환됨을 확인한다") {
                post.id shouldBe saved.id
                post.title shouldBe "title"
                post.content shouldBe "content"
                post.createdBy shouldBe "harris"
            }
            then("태그가 정상적으로 조회됨을 확인한다") {
                post.tags.size shouldBe 3
                post.tags[0] shouldBe "tag1"
                post.tags[1] shouldBe "tag2"
                post.tags[2] shouldBe "tag3"
            }
            then("좋아요 개수가 조회됨을 확인한다") {
                post.likeCount shouldBe 3
            }
        }
        When("게시물이 없을 때") {
            then("게시물을 찾을 수 없다는 예외가 발생") {
                shouldThrow<PostNotFoundException> { postService.getPost(9999L) }
            }
        }
        When("댓글 추가시") {
            commentRepository.save(Comment(content = "댓글 내용1", post = saved, createdBy = "댓글 작성자"))
            commentRepository.save(Comment(content = "댓글 내용2", post = saved, createdBy = "댓글 작성자"))
            commentRepository.save(Comment(content = "댓글 내용3", post = saved, createdBy = "댓글 작성자"))
            val post = postService.getPost(saved.id)
            then("댓글이 함께 조회됨을 확인") {
                post.comments.size shouldBe 3
                post.comments[0].content shouldBe "댓글 내용1"
                post.comments[1].content shouldBe "댓글 내용2"
                post.comments[2].content shouldBe "댓글 내용3"
                post.comments[0].createdBy shouldBe "댓글 작성자"
                post.comments[1].createdBy shouldBe "댓글 작성자"
                post.comments[2].createdBy shouldBe "댓글 작성자"
            }
        }
    }
    given("게시물 목록 조회시") {
        val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto())
        When("검색조건 없이 정상 조회시") {
            then("게시물 페이지가 반환된다") {
                postPage.number shouldBe 0
                postPage.size shouldBe 5
                postPage.content.size shouldBe 5
//                postPage.content[0].title shouldContain "title"
//                postPage.content[0].createdBy shouldContain "harris"
            }
        }
        When("타이틀로 검색") {
            val postPage1 = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(title = "title1"))
            then("차이틀에 해당하는 게시글이 반환된다") {
                postPage1.number shouldBe 0
                postPage1.size shouldBe 5
                postPage1.content.size shouldBe 5
//                postPage1.content[0].title shouldContain "title1"
//                postPage1.content[0].createdBy shouldContain "harris"
            }
        }
        When("작성자로 검색") {
            val postPage2 = postService.findPageBy(
                PageRequest.of(0, 5),
                PostSearchRequestDto(createdBy = "harris1")
            )
            then("작성자에 해당하는 게시글이 반환된다") {
                postPage2.number shouldBe 0
                postPage2.size shouldBe 5
                postPage2.content.size shouldBe 5
//                postPage2.content[0].title shouldContain "title"
//                postPage2.content[0].createdBy shouldBe "harris1"
            }
            then("첫번째 태그가 함께 조회됨을 확인한다") {
                postPage2.content.forEach {
                    it.firstTag shouldBe "tags1"
                }
            }
        }
        When("태그로 검색") {
            val postPage3 = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(tag = "tags5"))
            then("태그에 해당하는 게시물이 반환됩니다") {
                postPage3.number shouldBe 0
                postPage3.size shouldBe 5
                postPage3.content.size shouldBe 5
                postPage3.content[0].title shouldBe "title6"
                postPage3.content[1].title shouldBe "title7"
                postPage3.content[2].title shouldBe "title8"
                postPage3.content[3].title shouldBe "title9"
                postPage3.content[4].title shouldBe "title10"
            }
        }
        When("좋아요 추가되었을 때") {
            val postPage4 = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(tag = "tags5"))
            postPage4.content.forEach {
                likeService.createLike(it.id, "harris1")
                likeService.createLike(it.id, "harris2")
            }
            val likedPostPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(tag = "tags5"))
            then("좋아요 개수가 정상적으로 확인됨") {
                likedPostPage.content.forEach {
                    it.likeCount shouldBe 2
                }
            }
        }
    }
})
