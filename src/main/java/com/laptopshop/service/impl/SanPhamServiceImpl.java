package com.laptopshop.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.laptopshop.dto.SanPhamDto;
import com.laptopshop.dto.SearchSanPhamObject;
import com.laptopshop.entities.QSanPham;
import com.laptopshop.entities.SanPham;
import com.laptopshop.repository.DanhMucRepository;
import com.laptopshop.repository.HangSanXuatRepository;
import com.laptopshop.repository.SanPhamRepository;
import com.laptopshop.service.SanPhamService;
import com.querydsl.core.BooleanBuilder;

@Service
public class SanPhamServiceImpl implements SanPhamService {

	@Autowired
	private SanPhamRepository sanPhamRepo;

	@Autowired
	private DanhMucRepository danhMucRepo;

	@Autowired
	private HangSanXuatRepository hangSanXuatRepo;

	@PersistenceContext
	private EntityManager entityManager;

	@PostConstruct
	public void postConstruct()
	{
		SearchSanPhamObject searchSanPhamObject1 = new SearchSanPhamObject();
		searchSanPhamObject1.setDanhMucId("13");
		searchSanPhamObject1.setHangSXId("");
		searchSanPhamObject1.setDonGia("tren-20-trieu");
		List<SanPham> sanPhams = getAllSanPhamByFilterUsingCriteriaAPI(searchSanPhamObject1, 2, 5);
		for (SanPham sanPham : sanPhams)
		System.out.println(sanPham.getTenSanPham()+"-"+ sanPham.getThongTinChung()+"\n");

		// SanPham sanPham = getSanPhamByIdJpaCriteria();
		// System.out.println(sanPham.getTenSanPham()+"-"+ sanPham.getThongTinChung());
	}

	// đổi từ SanPhamDto sang đối tượng SanPham để add vào db
	public SanPham convertFromSanPhamDto(SanPhamDto dto) {
		SanPham sanPham = new SanPham();
		if (!dto.getId().equals("")) {
			sanPham.setId(Long.parseLong(dto.getId()));
		}
		sanPham.setTenSanPham(dto.getTenSanPham());
		sanPham.setCpu(dto.getCpu());
		sanPham.setDanhMuc(danhMucRepo.findById(dto.getDanhMucId()).get());
		sanPham.setHangSanXuat(hangSanXuatRepo.findById(dto.getNhaSXId()).get());
		sanPham.setDonGia(Long.parseLong(dto.getDonGia()));
		sanPham.setThietKe(dto.getThietKe());
		sanPham.setThongTinBaoHanh(dto.getThongTinBaoHanh());
		sanPham.setThongTinChung(dto.getThongTinChung());
		sanPham.setManHinh(dto.getManHinh());
		sanPham.setRam(dto.getRam());
		sanPham.setDungLuongPin(dto.getDungLuongPin());
		sanPham.setDonViKho(Integer.parseInt(dto.getDonViKho()));
		sanPham.setHeDieuHanh(dto.getHeDieuHanh());

		return sanPham;
	}

	@Override
	public SanPham save(SanPhamDto dto) {
		SanPham sp = convertFromSanPhamDto(dto);
		System.out.println(sp);
		return sanPhamRepo.save(sp);
	}

	@Override
	public SanPham update(SanPhamDto dto) {
		return sanPhamRepo.save(convertFromSanPhamDto(dto));
	}

	@Override
	public void deleteById(long id) {
		sanPhamRepo.deleteById(id);

	}

	@Override
	public Page<SanPham> getAllSanPhamByFilter(SearchSanPhamObject object, int page, int limit) {
		BooleanBuilder builder = new BooleanBuilder();
		String price = object.getDonGia();

		// sắp xếp theo giá
		Sort sort = Sort.by(Direction.ASC, "donGia"); // mặc định tăng dần
		if (object.getSapXepTheoGia().equals("desc")) { // giảm dần
			sort = Sort.by(Direction.DESC, "donGia");
		}

		if (!object.getDanhMucId().equals("") && object.getDanhMucId() != null) {
			builder.and(QSanPham.sanPham.danhMuc.eq(danhMucRepo.findById(Long.parseLong(object.getDanhMucId())).get()));
		}

		if (!object.getHangSXId().equals("") && object.getHangSXId() != null) {
			builder.and(QSanPham.sanPham.hangSanXuat
					.eq(hangSanXuatRepo.findById(Long.parseLong(object.getHangSXId())).get()));
		}

		// tim theo don gia
		switch (price) {
		case "duoi-2-trieu":
			builder.and(QSanPham.sanPham.donGia.lt(2000000));
			break;

		case "2-trieu-den-4-trieu":
			builder.and(QSanPham.sanPham.donGia.between(2000000, 4000000));
			break;

		case "4-trieu-den-6-trieu":
			builder.and(QSanPham.sanPham.donGia.between(4000000, 6000000));
			break;

		case "6-trieu-den-10-trieu":
			builder.and(QSanPham.sanPham.donGia.between(6000000, 10000000));
			break;

		case "tren-10-trieu":
			builder.and(QSanPham.sanPham.donGia.gt(10000000));
			break;

		default:
			break;
		}
		return sanPhamRepo.findAll(builder, PageRequest.of(page, limit, sort));
	}

	public List<SanPham> getSanPhamByIdJpaCriteria()
	{
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<SanPham> query = builder.createQuery(SanPham.class);
		Root<SanPham> root = query.from(SanPham.class);

		// return entityManager.createQuery(query.select(root)
		// .where(builder.gt(root.get("donGia"), 41099000))).getResultList();

		return entityManager.createQuery(query.select(root)
		.where(builder.like(root.get("tenSanPham"), "null"))).getResultList();
	}

	public List<SanPham> getAllSanPhamByFilterUsingCriteriaAPI(SearchSanPhamObject searchSanPhamObject
	,int page ,int limit)
	{
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<SanPham> query = builder.createQuery(SanPham.class);
		Root<SanPham> root = query.from(SanPham.class);

		ArrayList<Predicate> predicates = new ArrayList<>();
		if (searchSanPhamObject.getDanhMucId().length() != 0) 
			predicates.add(
				builder.equal(root.get("danhMuc")
				, danhMucRepo.findById(Long.parseLong(searchSanPhamObject.getDanhMucId())).get())
			);

		if (searchSanPhamObject.getHangSXId().length() != 0)
			predicates.add(
				builder.equal(root.get("hangSanXuat")
				, hangSanXuatRepo.findById(Long.parseLong(searchSanPhamObject.getHangSXId())).get())
			);

		String price = searchSanPhamObject.getDonGia();
		Predicate predicate3 = null;
		switch (price) {
			case "duoi-2-trieu":
				predicate3 = builder.lt(root.get("donGia"), 2000000);
				break;

			case "2-trieu-den-4-trieu":
				predicate3 = builder.between(root.get("donGia"), 2000000, 4000000);
				break;

			case "4-trieu-den-6-trieu":
				predicate3 = builder.between(root.get("donGia"), 4000000, 6000000);
				break;

			case "6-trieu-den-10-trieu":
				predicate3 = builder.between(root.get("donGia"), 6000000, 10000000);
				break;

			case "tren-10-trieu":
				predicate3 = builder.gt(root.get("donGia"), 10000000);
				break;

			case "tren-20-trieu":
				predicate3 = builder.gt(root.get("donGia"), 20000000);
			break;

			default:
				break;
			}
		if (predicate3 != null) predicates.add(predicate3);

		List<SanPham> sanPhams;
		if (searchSanPhamObject.getSapXepTheoGia().equals("desc"))
			sanPhams = entityManager.createQuery(query.select(root).where(predicates.toArray(new Predicate[] {}))
			.orderBy(builder.desc(root.get("donGia"))))
			.setFirstResult((page-1)*limit).setMaxResults(limit)
			.getResultList();
		else 
			sanPhams = entityManager.createQuery(query.select(root).where(predicates.toArray(new Predicate[] {}))
			.orderBy(builder.asc(root.get("donGia"))))
			.setFirstResult((page-1)*limit).setMaxResults(limit)
			.getResultList();

		return sanPhams;
	}

	public List<SanPham> getProductByIdQueryTest()
	{
		return sanPhamRepo.findRandomTest();
	}

	@Override
	public List<SanPham> getLatestSanPham() {
		return sanPhamRepo.findFirst12ByDanhMucTenDanhMucContainingIgnoreCaseOrderByIdDesc("Laptop");
	}

	public Iterable<SanPham> getSanPhamByTenSanPhamWithoutPaginate(SearchSanPhamObject object) {
		BooleanBuilder builder = new BooleanBuilder();
		int resultPerPage = 12;
		String[] keywords = object.getKeyword();
		String sort = object.getSort();
		String price = object.getDonGia();
		// Keyword
		builder.and(QSanPham.sanPham.tenSanPham.like("%" + keywords[0] + "%"));
		if (keywords.length > 1) {
			for (int i = 1; i < keywords.length; i++) {
				builder.and(QSanPham.sanPham.tenSanPham.like("%" + keywords[i] + "%"));
			}
		}
		// Muc gia
		switch (price) {
		case "duoi-2-trieu":
			builder.and(QSanPham.sanPham.donGia.lt(2000000));
			break;

		case "2-trieu-den-4-trieu":
			builder.and(QSanPham.sanPham.donGia.between(2000000, 4000000));
			break;

		case "4-trieu-den-6-trieu":
			builder.and(QSanPham.sanPham.donGia.between(4000000, 6000000));
			break;

		case "6-trieu-den-10-trieu":
			builder.and(QSanPham.sanPham.donGia.between(6000000, 10000000));
			break;

		case "tren-10-trieu":
			builder.and(QSanPham.sanPham.donGia.gt(10000000));
			break;

		default:
			break;
		}
		return sanPhamRepo.findAll(builder);
	}

	@Override
	public SanPham getSanPhamById(long id) {
		return sanPhamRepo.findById(id).get();
	}

	// Tim kiem san pham theo keyword, sap xep, phan trang, loc theo muc gia, lay 12
	// san pham moi trang
	@Override
	public Page<SanPham> getSanPhamByTenSanPham(SearchSanPhamObject object, int page, int resultPerPage) {
		BooleanBuilder builder = new BooleanBuilder();
//		int resultPerPage = 12;
		String[] keywords = object.getKeyword();
		String sort = object.getSort();
		String price = object.getDonGia();
		String brand = object.getBrand();
		String manufactor = object.getManufactor();
		// Keyword
		builder.and(QSanPham.sanPham.tenSanPham.like("%" + keywords[0] + "%"));
		if (keywords.length > 1) {
			for (int i = 1; i < keywords.length; i++) {
				builder.and(QSanPham.sanPham.tenSanPham.like("%" + keywords[i] + "%"));
			}
		}
		// Muc gia
		switch (price) {
		case "duoi-2-trieu":
			builder.and(QSanPham.sanPham.donGia.lt(2000000));
			break;

		case "2-trieu-den-4-trieu":
			builder.and(QSanPham.sanPham.donGia.between(2000000, 4000000));
			break;

		case "4-trieu-den-6-trieu":
			builder.and(QSanPham.sanPham.donGia.between(4000000, 6000000));
			break;

		case "6-trieu-den-10-trieu":
			builder.and(QSanPham.sanPham.donGia.between(6000000, 10000000));
			break;

		case "tren-10-trieu":
			builder.and(QSanPham.sanPham.donGia.gt(10000000));
			break;

		default:
			break;
		}

		// Danh muc va hang san xuat
		if (brand.length()>1) {
			builder.and(QSanPham.sanPham.danhMuc.tenDanhMuc.eq(brand));
		}
		if (manufactor.length()>1) {
			builder.and(QSanPham.sanPham.hangSanXuat.tenHangSanXuat.eq(manufactor));
		}

		// Sap xep
		if (sort.equals("newest")) {
			return sanPhamRepo.findAll(builder, PageRequest.of(page - 1, resultPerPage, Sort.Direction.DESC, "id"));
		} else if (sort.equals("priceAsc")) {
			return sanPhamRepo.findAll(builder, PageRequest.of(page - 1, resultPerPage, Sort.Direction.ASC, "donGia"));
		} else if (sort.equals("priceDes")) {
			return sanPhamRepo.findAll(builder, PageRequest.of(page - 1, resultPerPage, Sort.Direction.DESC, "donGia"));
		}
		return sanPhamRepo.findAll(builder, PageRequest.of(page - 1, resultPerPage));
	}

	public List<SanPham> getAllSanPhamByList(Set<Long> idList) {
		return sanPhamRepo.findByIdIn(idList);
	}

	@Override
	public Page<SanPham> getSanPhamByTenSanPhamForAdmin(String tenSanPham, int page, int size) {
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(QSanPham.sanPham.tenSanPham.like("%" + tenSanPham + "%"));
		return sanPhamRepo.findAll(builder, PageRequest.of(page, size));
	}
	
	
	@Override
	public Iterable<SanPham> getSanPhamByTenDanhMuc(String brand) {
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(QSanPham.sanPham.danhMuc.tenDanhMuc.eq(brand));
		return sanPhamRepo.findAll(builder);
	}
	
	@Override
	public Page<SanPham> getSanPhamByBrand(SearchSanPhamObject object, int page, int resultPerPage) {
		BooleanBuilder builder = new BooleanBuilder();
		String price = object.getDonGia();
		String brand = object.getBrand();
		String manufactor = object.getManufactor();
		String os = object.getOs();
		String ram = object.getRam();
		String pin = object.getPin();
		// Muc gia
		switch (price) {
		case "duoi-2-trieu":
			builder.and(QSanPham.sanPham.donGia.lt(2000000));
			break;

		case "2-trieu-den-4-trieu":
			builder.and(QSanPham.sanPham.donGia.between(2000000, 4000000));
			break;

		case "4-trieu-den-6-trieu":
			builder.and(QSanPham.sanPham.donGia.between(4000000, 6000000));
			break;

		case "6-trieu-den-10-trieu":
			builder.and(QSanPham.sanPham.donGia.between(6000000, 10000000));
			break;

		case "tren-10-trieu":
			builder.and(QSanPham.sanPham.donGia.gt(10000000));
			break;

		default:
			break;
		}

		// Danh muc va hang san xuat
		if (brand.length()>1) {
			builder.and(QSanPham.sanPham.danhMuc.tenDanhMuc.eq(brand));
		}
		if (manufactor.length()>1) {
			builder.and(QSanPham.sanPham.hangSanXuat.tenHangSanXuat.eq(manufactor));
		}
		if (os.length()>1) {
			builder.and(QSanPham.sanPham.heDieuHanh.like("%"+os+"%"));
		}
		if (ram.length()>1) {
			builder.and(QSanPham.sanPham.ram.like("%"+ram+"%"));
		}
		if (pin.length()>1) {
			builder.and(QSanPham.sanPham.dungLuongPin.eq(pin));
		}

		return sanPhamRepo.findAll(builder, PageRequest.of(page - 1, resultPerPage));
	}

	@Override
	public Page<SanPham> getAllSanPhamByFilter(int page, int limit) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getAllSanPhamByFilter'");
	}
}
