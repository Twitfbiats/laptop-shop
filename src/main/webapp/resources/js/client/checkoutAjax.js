var paypal_sdk_url = 'https://www.paypal.com/sdk/js'
var home_url = "http://localhost:8080/laptopshop"
var get_client_id_url_prefix = "/pay/get-client-id"
var create_order_url_prefix = "/pay/create-order"
var capture_order_url_prefix = "/pay/capture-order/"
var pay_redirect_url_prefix = "/thankyou"
var client_id

var hoTenNguoiNhan = $("#hoTenNguoiNhan")
var sdtNhanHang = $("#sdtNhanHang")
var diaChiNhan = $("#diaChiNhan")
var alertMsg = $('#alertMsg')


init()
calculateOrder()

async function init()
{
	const script = document.createElement('script')
	getClientId().then(client_id => 
	{
		console.log(client_id)
		script.src = paypal_sdk_url + `?client-id=${client_id}&components=buttons`
		document.head.appendChild(script)
		
		script.onload = function()
		{
			paypal.Buttons
			({
				style: 
				{
					layout: 'vertical',
					color:  'gold',
					shape:  'rect',
					label:  'checkout'
				},
				onInit: function(data, actions)
				{
					actions.disable();

					setInputListener(actions)
				},
				createOrder()
				{
					return fetch(home_url + create_order_url_prefix, 
					{
						method: "GET",
					})
					.then((response) => response.json())
					.then((order) => {return order.id;})
				},
				onApprove(data)
				{
					const donhang =
					{
						hoTenNguoiNhan: hoTenNguoiNhan.val(),
						sdtNhanHang: sdtNhanHang.val(),
						diaChiNhan: diaChiNhan.val()
					}

					fetch(home_url + capture_order_url_prefix + data.orderID
					+ "?hoTenNguoiNhan=" + donhang.hoTenNguoiNhan
					+ "&sdtNhanHang=" + donhang.sdtNhanHang
					+ "&diaChiNhan=" + donhang.diaChiNhan,
					{
						method: "GET",
						redirect: 'follow'
					})
					.then((response) => response.text())
					.then((text) => 
					{
						console.log(text)

						if (text == 'COMPLETED')
						{
							window.location.href = home_url + pay_redirect_url_prefix;
						}
					})
				}
			}).render('#paypal-container');
		}
	})
}

function getClientId()
{
	return fetch(home_url + get_client_id_url_prefix)
	.then(response => 
	{
		return response.text()
	})
}

function validateInput(actions)
{
	if (hoTenNguoiNhan.val() == '' || sdtNhanHang.val() == '' || diaChiNhan.val() == '')
	{
		actions.disable()
		toggleErrorMessage(true)
	}
	else
	{
		actions.enable()
		toggleErrorMessage(false)
	}
}

function toggleErrorMessage(on)
{
	if (on) alertMsg.removeAttr('hidden')
	else alertMsg.attr('hidden', true)
}

function setInputListener(actions)
{
	hoTenNguoiNhan.on('change', function() {validateInput(actions)})
	sdtNhanHang.on('change', function() {validateInput(actions)})
	diaChiNhan.on('change', function() {validateInput(actions)})
}

function calculateOrder()
{
	var element = document.getElementsByClassName("total");
	var res = 0;
	for (i = 0; i < element.length; i++) {
		res = res + parseInt(element[i].textContent);
	}
	var element2 = document.getElementById("ordertotal");
	
	resConvert = accounting.formatMoney(res);
	element2.innerHTML = resConvert+ " VND";
	var element3 = document.getElementById("tongGiaTri");
	element3.setAttribute("value",res);
	if(res == 0)
	{
		document.getElementById("submit").disabled = true;		
	}	
}
