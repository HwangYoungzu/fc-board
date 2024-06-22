package com.fastcampus.fcboard.repository

import com.fastcampus.fcboard.domain.Like
import org.springframework.data.jpa.repository.JpaRepository

// countByPostId : JpaRepository 내장 함수, 카운팅 해준다
interface LikeRepository : JpaRepository<Like, Long> {
    fun countByPostId(postId: Long): Long
}
