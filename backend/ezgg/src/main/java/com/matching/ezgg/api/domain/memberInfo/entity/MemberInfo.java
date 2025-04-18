package com.matching.ezgg.api.domain.memberInfo.entity;

import java.util.List;

import com.matching.ezgg.common.MatchIdConvert;
import com.matching.ezgg.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_info")
public class MemberInfo extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "member_id", unique = false, nullable = false)
	private Long memberId;

	@Column(name = "riot_username", unique = false, nullable = false)
	private String riotUsername;

	@Column(name = "riot_tag", unique = false, nullable = false)
	private String riotTag;

	@Column(name = "puuid", unique = true, nullable = true)
	private String puuid;

	@Column(name = "last_played_match_id", unique = false, nullable = true)
	private String lastPlayedMatchId;

	@Column(name = "match_ids", unique = false, nullable = true)
	@Convert(converter = MatchIdConvert.class)
	private List<String> matchIds;//TODO 테스트 케이스 작성 필요

	@Column(name = "tier", unique = false, nullable = true)
	private String tier;

	@Column(name = "wins", unique = false, nullable = true)
	private Integer wins;

	@Column(name = "losses", unique = false, nullable = true)
	private Integer losses;

	@Builder
	public MemberInfo(Long memberId, String riotUsername, String riotTag, String puuid, String lastPlayedMatchId,
		List<String> matchIds, String tier, Integer wins, Integer losses) {
		this.memberId = memberId;
		this.riotUsername = riotUsername;
		this.riotTag = riotTag;
		this.puuid = puuid;
		this.lastPlayedMatchId = lastPlayedMatchId;
		this.matchIds = matchIds;
		this.tier = tier;
		this.wins = wins;
		this.losses = losses;
	}
}
