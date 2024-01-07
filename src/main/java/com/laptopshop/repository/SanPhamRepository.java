package com.laptopshop.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;


import com.laptopshop.entities.SanPham;

public interface SanPhamRepository extends JpaRepository<SanPham, Long>, QuerydslPredicateExecutor<SanPham>{

	@Query(value = "SELECT * FROM san_pham WHERE san_pham.ma_danh_muc = (SELECT danh_muc.id FROM danh_muc WHERE danh_muc.ten_danh_muc = \"VGA\") && san_pham.ma_hang_sx = (SELECT hang_san_xuat.id FROM hang_san_xuat WHERE hang_san_xuat.ten_hang_san_xuat = \"ASUS\") && san_pham.don_gia >= 10000000 ORDER BY san_pham.don_gia ASC;", nativeQuery = true)
	List<SanPham> findRandomTest();

	// @Query(value = "SELECT s FROM san_pham WHERE s.ma_danh_muc = ?1 && s.ma_hang_sx = ?2")
	// List<SanPham> findUsingJpqlQueryTest(String category, String manufacturer);
	
	List<SanPham> findFirst12ByDanhMucTenDanhMucContainingIgnoreCaseOrderByIdDesc(String dm);
	List<SanPham> findByIdIn(Set<Long> idList);

	

}
