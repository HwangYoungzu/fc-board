package com.fastcampus.fcboard.repository

import com.fastcampus.fcboard.domain.QPost.post
import com.fastcampus.fcboard.domain.QTag.tag
import com.fastcampus.fcboard.domain.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport

interface TagRepository : JpaRepository<Tag, Long>, CustomTagRepository {
    fun findByPostId(postId: Long): List<Tag>
}

interface CustomTagRepository {
    fun findPageBy(pageRequest: Pageable, tagName: String): Page<Tag>
}

class CustomTagRepositoryImpl : CustomTagRepository, QuerydslRepositorySupport(Tag::class.java) {
    override fun findPageBy(pageRequest: Pageable, tagName: String): Page<Tag> {
        // tag 라는 큐 클래스 에서
        return from(tag)
            // @@@@@@@@@@@@@@@ 이 fetchJoin으로 tag 테이블과 post 테이블을 조회하여 한번에 select!!
            // tag조회에서 발생하는 N+1 문제 해결합니다
            .join(tag.post, post).fetchJoin()
            // tagName과 같은 name 을 가지고 있는 행을
            .where(tag.name.eq(tagName))
            // tag 와 연관되어 있는 post 라는 테이블의 createdAt의 내림차순으로 정렬하고,
            .orderBy(tag.post.createdAt.desc())
            // offset과 limit
            // limit : 결과 중 처음부터 몇개만 가져오기
            // offset : 결과를 어디서부터 가져올지
            // >> limit a offset b --> b+1 부터 b+a 까지 출력
            .offset(pageRequest.offset)
            .limit(pageRequest.pageSize.toLong())
            .fetchResults()
            .let {
                PageImpl(it.results, pageRequest, it.total)
            }
        // 혹은, application-loacl.yml 파일에서
        // Hibernate에서 제공하는 설정인 default_batch_fetch_size 설정을 해주어 최적화를 진행합니다
        // 지연로딩으로 발생해야 하는 쿼리를 해당 개수만큼 IN절로 한번에 모아보내는 기능입니다
        // ToOne 관계에서는 최대한 fetchJoin으로 활용하고,
        // 그 외 OneToMany 등의 관계에서는 batch_fetch_size 등을 통해 최적화 하는 것이 좋습니다
        // 다만, fetchJoin을 쓰면 한번에 join한 함수를 불러오기 때문에 테이블 크기가 너무 커질 수 있다는 위험이 있고,
        // FETCH JOIN은 관련 엔티티를 한 번의 쿼리로 가져오기 때문에, 메인 엔티티의 각 행이 관련 엔티티의 각 행과 결합되어
        // 중복된 데이터가 생성될 수도 있고, 페이징을 동시에 사용할 때 성능이 저하될 수 있습니다
        // default_batch_fetch_size를 너무 크게 설정하면 한 번에 많은 데이터를 메모리에 로드하게 됩니다.
        // 이는 메모리 사용량을 증가시키고, 메모리 부족 문제를 일으킬 수 있습니다.
        // 특히, 엔티티가 메모리를 많이 차지하거나 동시에 여러 배치 페치가 실행될 경우 문제가 심각해질 수 있습니다.
    }
}
