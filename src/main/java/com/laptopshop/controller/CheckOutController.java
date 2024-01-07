package com.laptopshop.controller;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.laptopshop.dto.CurrencyRateDTO;
import com.laptopshop.entities.ChiMucGioHang;
import com.laptopshop.entities.ChiTietDonHang;
import com.laptopshop.entities.DonHang;
import com.laptopshop.entities.GioHang;
import com.laptopshop.entities.NguoiDung;
import com.laptopshop.entities.SanPham;
import com.laptopshop.service.ChiMucGioHangService;
import com.laptopshop.service.ChiTietDonHangService;
import com.laptopshop.service.DonHangService;
import com.laptopshop.service.GioHangService;
import com.laptopshop.service.NguoiDungService;
import com.laptopshop.service.SanPhamService;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
@SessionAttributes("loggedInUser")
public class CheckOutController {
	
	@Autowired
	private SanPhamService sanPhamService;
	@Autowired
	private NguoiDungService nguoiDungService;
	@Autowired
	private GioHangService gioHangService;
	@Autowired
	private ChiMucGioHangService chiMucGioHangService;
	@Autowired
	private DonHangService donHangService;
	@Autowired
	private ChiTietDonHangService chiTietDonHangService;

	@Autowired
    private RestTemplate restTemplate;

    private JsonParser jsonParser = JsonParserFactory.getJsonParser();

	@ModelAttribute("loggedInUser")
	public NguoiDung loggedInUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return nguoiDungService.findByEmail(auth.getName());
	}
	
	public NguoiDung getSessionUser(HttpServletRequest request) {
		return (NguoiDung) request.getSession().getAttribute("loggedInUser");
	}
	
	@GetMapping("/checkout")
	public String checkoutPage(HttpServletRequest req, Model model) {
		NguoiDung currentUser = getSessionUser(req);
		Map<Long,String> quanity = new HashMap<Long,String>();
		List<SanPham> listsp = new ArrayList<SanPham>();

		GioHang g = gioHangService.getGioHangByNguoiDung(currentUser);

		List<ChiMucGioHang> listchimuc = chiMucGioHangService.getChiMucGioHangByGioHang(g);

		for(ChiMucGioHang c: listchimuc)
		{
			
			listsp.add(c.getSanPham());
			quanity.put(c.getSanPham().getId(), Integer.toString(c.getSo_luong()));
							
		}
		
		model.addAttribute("cart",listsp);
		model.addAttribute("quanity",quanity);
		model.addAttribute("user", currentUser);
		model.addAttribute("donhang", new DonHang());
		
		return "client/checkout";
	}

	@PostMapping("/complete-order")
	public String completeOrder(@ModelAttribute("donhang") DonHang donhang ,HttpServletRequest req,HttpServletResponse response ,Model model) 
	{
		SaveOrder(donhang, req, response, model, (byte)0);
		
		return "redirect:/thankyou";
	}
	
	
	@GetMapping(value="/thankyou")
	public String thankyouPage(HttpServletRequest req ,Model model)
	{
		NguoiDung currentUser = getSessionUser(req);
		DonHang donhang = donHangService.findLatestDonHangByMaNguoiDat(currentUser.getId());
		Map<Long,String> quanity = new HashMap<Long,String>();
		List<SanPham> listsp = new ArrayList<SanPham>();

		List<ChiTietDonHang> chiTietDonHangs = donhang.getDanhSachChiTiet();
		for(ChiTietDonHang c: chiTietDonHangs)
		{		
			listsp.add(c.getSanPham());
			quanity.put(c.getSanPham().getId(), Integer.toString(c.getSoLuongDat()));
		}					
		
		model.addAttribute("donhang",donhang);
		model.addAttribute("cart",listsp);
		model.addAttribute("quanity",quanity);
		
		return "client/thankYou";
	}

	public void SaveOrder(DonHang donhang ,HttpServletRequest req,HttpServletResponse response ,Model model, byte status)
	{
		if (status == 1) donhang.setGhiChu("Đã thanh toán");
		else donhang.setGhiChu("Thanh toán khi nhận hàng");
		donhang.setNgayDatHang(new Date());
		donhang.setTrangThaiDonHang("Đang chờ giao");

		NguoiDung currentUser = getSessionUser(req);
		Map<Long,String> quanity = new HashMap<Long,String>();
		List<SanPham> listsp = new ArrayList<SanPham>();
		List<ChiTietDonHang> listDetailDH = new ArrayList<ChiTietDonHang>();
	
		donhang.setNguoiDat(currentUser);
		System.out.println(donhang.getId());
		DonHang d = donHangService.save(donhang);
		GioHang g = gioHangService.getGioHangByNguoiDung(currentUser);
		List<ChiMucGioHang> listchimuc = chiMucGioHangService.getChiMucGioHangByGioHang(g);
		for(ChiMucGioHang c: listchimuc)
		{			
			ChiTietDonHang detailDH = new ChiTietDonHang();
			detailDH.setSanPham(c.getSanPham());
			detailDH.setDonGia(c.getSo_luong()*c.getSanPham().getDonGia());	
			detailDH.setSoLuongDat(c.getSo_luong());
			detailDH.setDonHang(d);
			listDetailDH.add(detailDH);		
			
			listsp.add(c.getSanPham());
			quanity.put(c.getSanPham().getId(), Integer.toString(c.getSo_luong()));
		}			
			
		chiTietDonHangService.save(listDetailDH);
		
		cleanUpAfterCheckOut(req);
		model.addAttribute("donhang",donhang);
		model.addAttribute("cart",listsp);
		model.addAttribute("quanity",quanity);
	}
	
	public void cleanUpAfterCheckOut(HttpServletRequest request)
	{
		NguoiDung currentUser = getSessionUser(request);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		GioHang g = gioHangService.getGioHangByNguoiDung(currentUser);
		List<ChiMucGioHang> c = chiMucGioHangService.getChiMucGioHangByGioHang(g);
		chiMucGioHangService.deleteAllChiMucGiohang(c);
	}
	
	@Value("${paypal.token-url}")
    private String tokenUrl;

    @Value("${paypal.order-url}")
    private String orderUrl;

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

	@GetMapping("/pay/get-access-token")
    public ResponseEntity<String> paypalAccessToken()
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        RequestEntity<String> requestEntity
        = RequestEntity.post(tokenUrl).headers(headers).body("grant_type=client_credentials");

        var res = restTemplate.exchange(requestEntity, String.class).getBody();
        Map<String, Object> resMap = jsonParser.parseMap(res);

        return ResponseEntity.ok(resMap.get("access_token") + "");
    }
	
	@GetMapping("/pay/get-client-id")
    public ResponseEntity<String> paypalClientId()
    {
        return ResponseEntity.ok(clientId);
    }

    @GetMapping("/pay/create-order")
    public ResponseEntity<String> paypalCreateOrder()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        NguoiDung nguoiDung = nguoiDungService.findByEmail(authentication.getName());

		if(authentication == null || authentication.getPrincipal() == "anonymousUser") return ResponseEntity.badRequest().body("You need to login");

		List<ChiMucGioHang> chiMucGioHangs = chiMucGioHangService.getChiMucGioHangByGioHang(gioHangService.getGioHangByNguoiDung(nguoiDung));
		SanPham sanPham;
		double giaSanPham;
		int soLuong;
		double currencyRate = ReadCurrencyRate();
		double item_total_value = 0;
        var access_token = restTemplate.getForObject("http://localhost:8080/laptopshop/pay/get-access-token", String.class);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(access_token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject ancestor = new JSONObject();

        JSONArray purchase_units = new JSONArray();
        ancestor.put("purchase_units", purchase_units);

        JSONObject purchase_unit = new JSONObject();
        purchase_units.put(purchase_unit);
            JSONArray items = new JSONArray();
            purchase_unit.put("items", items);
				for (ChiMucGioHang cmgh : chiMucGioHangs)
				{
					sanPham = cmgh.getSanPham();
					giaSanPham = Math.ceil(sanPham.getDonGia() * currencyRate);
					soLuong = cmgh.getSo_luong();

					JSONObject item = new JSONObject();
					items.put(item);
					item.put("name", sanPham.getTenSanPham());
					item.put("quantity", soLuong + "");
					item.put("description", "...");
					JSONObject unit_amount = new JSONObject();
					item.put("unit_amount", unit_amount);
					unit_amount.put("currency_code", "USD");
					unit_amount.put("value", giaSanPham + "");

					item_total_value += giaSanPham * soLuong;
				};
			JSONObject amount = new JSONObject();
            purchase_unit.put("amount", amount);
            amount.put("currency_code", "USD");
            amount.put("value", item_total_value + "");
                JSONObject breakdown = new JSONObject();
                amount.put("breakdown", breakdown);
                    JSONObject item_total = new JSONObject();
                    breakdown.put("item_total", item_total);
                    item_total.put("currency_code", "USD");
                    item_total.put("value", item_total_value + "");
            purchase_unit.put("description", "Laptop HUST 2023 SHOP");
        
        ancestor.put("intent", "CAPTURE");

		System.out.println(ancestor.toString());

        RequestEntity<String> requestEntity
        = RequestEntity.post(orderUrl).headers(headers).body(ancestor.toString());

        return restTemplate.exchange(requestEntity, String.class);
    }

	@Value("${paypal.capture-url-suffix}") private String captureUrlSuffix;
	@Value("${paypal.capture-url-prefix}") private String captureUrlPrefix;
	@GetMapping("/pay/capture-order/{id}")
	public ResponseEntity<String> paypalCaptureOrder(@PathVariable String id, @RequestParam("hoTenNguoiNhan") String hoTenNguoiNhan,
	@RequestParam("sdtNhanHang") String sdtNhanHang, @RequestParam("diaChiNhan") String diaChiNhan
	,HttpServletRequest req,HttpServletResponse response ,Model model)
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null || authentication.getPrincipal() == "anonymousUser") return ResponseEntity.badRequest().body("You need to login");

		var access_token = restTemplate.getForObject("http://localhost:8080/laptopshop/pay/get-access-token", String.class);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(access_token);
        headers.setContentType(MediaType.APPLICATION_JSON);

		RequestEntity<Void> requestEntity
        = RequestEntity.post(captureUrlSuffix + id + captureUrlPrefix).headers(headers).build();

		var res = restTemplate.exchange(requestEntity, String.class).getBody();
		JSONObject jsonObject = new JSONObject(res);
		if (jsonObject.getString("status").equals("COMPLETED"))
		{
			DonHang donhang = new DonHang();
			donhang.setHoTenNguoiNhan(hoTenNguoiNhan);
			donhang.setSdtNhanHang(sdtNhanHang);
			donhang.setDiaChiNhan(diaChiNhan);
			SaveOrder(donhang, req, response, model, (byte)1);
			return ResponseEntity.ok("COMPLETED");
		}

		return ResponseEntity.ok("ERROR");
	}

	@Value("${custom.currency_file_path}")
    private String currencyRateFilePath;

	@Value("${custom.currency_X-Rapidapi-Key}")
	private String XRapidapiKey;

	@Value("${custom.currency_X-Rapidapi-Host}")
	private String XRapidapiHost;

	@Value("${custom.currency_api_url}")
	private String apiUrl;

	@Value("${custom.currency_from}")
	private String from;

	@Value("${custom.currency_to}")
	private String to;

	@Value("${custom.currency_q}")
	private String q;

	/* Check for currency rate every 24 hrs */
	@Scheduled(fixedDelay = 24, timeUnit = TimeUnit.HOURS)
	public void ScheduleGetCurrencyRate() 
	{
		try 
		{
			HttpHeaders headers = new HttpHeaders();
			headers.set("X-Rapidapi-Key", XRapidapiKey);
			headers.set("X-Rapidapi-Host", XRapidapiHost);

			RequestEntity<Void> requestEntity
			= RequestEntity.get(apiUrl + "?from=" + from + "&to=" + to + "&q=" + q).headers(headers).build();

			var res = restTemplate.exchange(requestEntity, String.class).getBody();
			CurrencyRateDTO currencyRateDTO = new CurrencyRateDTO(from, to, res);
			WriteCurrencyRate(currencyRateDTO);	
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public void WriteCurrencyRate(CurrencyRateDTO currencyRateDTO) throws IOException
	{
		Gson gson = new Gson();
		FileWriter fileWriter = new FileWriter(currencyRateFilePath);
		gson.toJson(currencyRateDTO, fileWriter);
		fileWriter.close();
	}

	public double ReadCurrencyRate()
	{
		try 
		{
			JsonElement jsonElement = com.google.gson.JsonParser.parseReader(new FileReader(currencyRateFilePath));
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			var value = jsonObject.get("value").getAsDouble();

			return value;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return 0;
	}
}
