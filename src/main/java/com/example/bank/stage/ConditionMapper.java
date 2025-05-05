package com.example.bank.stage;

import com.example.bank.stage.Condition.RankChangeCondition;
import com.example.bank.stage.Condition.StageCondition;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ConditionMapper {

	private final JdbcClient jdbcClient;

	public ConditionMapper(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public List<Condition> findAll() {
		return this.jdbcClient
			.sql("""
					-- Retrieve all condition information in a single query, including both stage_conditions and rank_change_conditions
					SELECT
					    c.id,
					    c.condition_type,
					    c.condition_name,
					    c.condition_category,
					    c.valid_from,
					    c.valid_to,

					    -- Stage condition specific fields (null for rank change conditions)
					    sc.stage_code,
					    sc.min_value,
					    sc.max_value,

					    -- Rank change condition specific fields (null for stage conditions)
					    rc.threshold_value,
					    rc.rank_change_levels
					FROM
					    conditions c
					LEFT OUTER JOIN
					    stage_conditions sc ON c.id = sc.id
					LEFT OUTER JOIN
					    rank_change_conditions rc ON c.id = rc.id
					WHERE valid_from <= CURRENT_DATE AND valid_to >= CURRENT_DATE
					ORDER BY
					    c.condition_category,
					    c.condition_type
					""")
			.query(ConditionRowMapper.INSTANCE)
			.list();
	}

	private enum ConditionRowMapper implements RowMapper<Condition> {

		INSTANCE;

		@Override
		public Condition mapRow(ResultSet rs, int rowNum) throws SQLException {
			int id = rs.getInt("id");
			ConditionType conditionType = ConditionType.valueOf(rs.getString("condition_type"));
			String conditionName = rs.getString("condition_name");
			ConditionCategory conditionCategory = ConditionCategory.valueOf(rs.getString("condition_category"));
			return switch (conditionCategory) {
				case STAGE -> mapStageCondition(rs, id, conditionType, conditionName);
				case RANK_CHANGE -> mapRankChangeCondition(rs, id, conditionType, conditionName);
			};
		}

		private StageCondition mapStageCondition(ResultSet rs, int id, ConditionType conditionType,
				String conditionName) throws SQLException {
			StageCode stageCode = StageCode.valueOf(rs.getString("stage_code"));
			BigDecimal minValue = rs.getBigDecimal("min_value");
			BigDecimal maxValue = rs.getBigDecimal("max_value");
			LocalDate validFrom = rs.getObject("valid_from", LocalDate.class);
			LocalDate validTo = rs.getObject("valid_to", LocalDate.class);
			return new StageCondition(id, conditionType, conditionName, stageCode, minValue, maxValue, validFrom,
					validTo);
		}

		private RankChangeCondition mapRankChangeCondition(ResultSet rs, int id, ConditionType conditionType,
				String conditionName) throws SQLException {
			BigDecimal thresholdValue = rs.getBigDecimal("threshold_value");
			int rankChangeLevels = rs.getInt("rank_change_levels");
			LocalDate validFrom = rs.getObject("valid_from", LocalDate.class);
			LocalDate validTo = rs.getObject("valid_to", LocalDate.class);
			return new RankChangeCondition(id, conditionType, conditionName, thresholdValue, rankChangeLevels,
					validFrom, validTo);
		}

	}

}
