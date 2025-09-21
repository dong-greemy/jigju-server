package com.jigju.server.location.repository;

import com.jigju.server.location.entity.HaengjeongEmdCode;
import com.jigju.server.location.entity.EmdOfficeLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmdOfficeLocationRepository extends JpaRepository<HaengjeongEmdCode, Long> {
//    @Query(
//        value = """
//            SELECT * FROM administrative_emd a
//            where a.emd REGEXP '[0-9]'
//              and a.emd collate utf8mb4_general_ci not in (
//                    select
//                        case
//                            when right(emd_office, 4) = '주민센터' then left(emd_office, CHAR_LENGTH(emd_office) - 4)
//                            when right(emd_office, 4) = '복지센터' then left(emd_office, CHAR_LENGTH(emd_office) - 6)
//                            when right(emd_office, 10) = '주민센터(임시청사)' then left(emd_office, CHAR_LENGTH(emd_office) - 10)
//                            when right(emd_office, 10) = '복지센터(임시청사)' then left(emd_office, CHAR_LENGTH(emd_office) - 12)
//                            else emd_office
//                        end as turncated_name
//                    from emd_office_location b
//                    where b.emd_office REGEXP'[0-9]'
//                )
//            order by province, emd;
//
//
//            """,
//        nativeQuery = true
//    )
//    List<HaengjeongEmdCode> findUnmathedEmds();

    @Query(value = """
    SELECT * FROM emd_office_location
    WHERE province = LEFT(:province, 2)
      AND district = :district
      AND (
          emd_office LIKE CONCAT('%', :emdName, '%')
      )
    """, nativeQuery = true)
    EmdOfficeLocation findMatchedEmdOffice(
            @Param("province") String province,
            @Param("district") String district,
            @Param("emdName") String emdName
    );

}
