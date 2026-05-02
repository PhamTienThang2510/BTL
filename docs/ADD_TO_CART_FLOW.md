# Add To Cart Flow (Phan tich tu bundle production)

## 1) Ngu canh va gioi han
- Workspace hien tai chi co bundle build (`dist/assets/index-3SsP24ob.js`), khong co source `src/`.
- Toan bo logic da bi minify va dồn vao 1 line lon. Vi vay tham chieu dong chu yeu la line 16 trong file bundle.
- Ten ham minify van giu duoc cac symbol nghiep vu quan trong (`Nf`, `Dr`, `Ug`, `Bg`, `Hg`, `KE`, `kE`, ...).

## 2) Cac thanh phan tham gia

### 2.1 API client (Axios)
- Tao instance Axios:
  - `Ye=qe.create({baseURL:"http://localhost:3000",headers:{"Content-Type":"application/json"}})`
- Request interceptor:
  - Doc `token` tu `localStorage` va gan `Authorization: Bearer <token>`.
- Response interceptor:
  - Pass through thanh cong, reject loi.

Y nghia:
- Moi call cart se mac dinh ve host API `http://localhost:3000`.
- Neu chua login/khong co token, API van duoc goi nhung co the bi 401 tu backend.

### 2.2 Redux async thunk cho cart
- `Dr = cart/getCart`:
  - `GET /carts/${userId}`
- `Xo = cart/fetchCart`:
  - `GET /carts`
- `Nf = cart/addToCart`:
  - Thu `POST /cart/add` truoc
  - Neu fail thi fallback sang `POST /carts`
- `Ug = cart/updateCart`:
  - `PUT /carts/item` voi payload updates
- `Bg = cart/removeCartItem`:
  - `DELETE /carts/item/${id}`

Y nghia:
- App dang ho tro 2 backend contract khac nhau cho add cart (`/cart/add` va `/carts`).
- Update/remove item phu thuoc endpoint `/carts/item` va `/carts/item/:id`.

### 2.3 Cart slice (`Hg`)
Initial state:
- `items: []`
- `status: "idle"`
- `error: null`
- `user_cart: {}`

Reducer quan trong:
- `Nf.fulfilled`:
  - Tim item trung id trong `items`.
  - Neu co: tang `quantity`.
  - Neu khong: push item moi vao `items`.
- `Ug.fulfilled`:
  - Update item theo `cart_item_id` cho ca `items` va `user_cart.cart_items`.
- `Bg.fulfilled`:
  - Remove theo `id` khoi `items` va `user_cart.cart_items`.

Y nghia:
- State cart dang co 2 nguon du lieu song song (`items` va `user_cart.cart_items`).
- Header dang dung `items` de dem so luong, trong khi man hinh cart dung `user_cart.cart_items`.

## 3) Luong Add to Cart hien tai

### Buoc 1: User thao tac UI
Co 2 diem lien quan:
1. Home listing (`function kE`):
- Co truyen prop `onAddToCart` vao card `t0` va dispatch `Nf(...)`.
- Tuy nhien trong `function t0`, prop nay KHONG duoc su dung; button hien tai chi navigate sang chi tiet san pham.

2. Product detail (`function KE`):
- Nut `Add to Cart` goi truc tiep:
- `dispatch(Nf({ product_id, product_name, price, quantity, product_media }))`

=> Ket luan: Add to cart that su dang chay on dinh tai trang chi tiet san pham (`KE`), khong phai button card danh sach (`t0`).

### Buoc 2: Thunk `Nf` xu ly API
Payload gui len:
- `product_id`
- `product_name`
- `price`
- `quantity`
- `product_media`

Trinh tu API:
1. `POST /cart/add` voi body payload
2. Neu throw error: fallback `POST /carts` voi cung payload

### Buoc 3: Reducer cap nhat state
- Nhan `i.payload` tu API.
- Neu tim thay item trung id trong `state.items` thi cong don so luong.
- Neu khong tim thay thi them item moi.

### Buoc 4: UI phan ung
- Header cart badge doc tu `state.cart.items.reduce((sum, item) => sum + quantity, 0)`.
- Neu API tra ve object khong dung schema (khong co `id`, `quantity`) thi badge co the sai.

## 4) API contract de backend map dung

## 4.1 De xuat request body cho Add To Cart
```json
{
  "product_id": 123,
  "quantity": 2,
  "product_name": "Ao thun",
  "price": 19.99,
  "product_media": [{ "media_url": "/uploads/a.jpg" }]
}
```

## 4.2 De xuat response body cho Add To Cart
Nen tra ve item da duoc chuan hoa voi cac field sau de reducer hoat dong dung:
```json
{
  "id": 123,
  "cart_item_id": 987,
  "product_id": 123,
  "product_name": "Ao thun",
  "quantity": 2,
  "price": 19.99,
  "product_media": [{ "media_url": "/uploads/a.jpg" }]
}
```

Ly do:
- Reducer `Nf.fulfilled` dang tim theo `id`.
- Reducer update/remove dang tim theo `cart_item_id`.
- Neu backend khong tra day du 2 khoa nay, luong cap nhat state se khong dong bo.

## 4.3 Endpoint map hien tai cua frontend
- `GET /carts/:userId` -> lay gio user
- `GET /carts` -> lay cart chung / fallback
- `POST /cart/add` -> them vao gio (uu tien)
- `POST /carts` -> them vao gio (fallback)
- `PUT /carts/item` -> cap nhat so luong item
- `DELETE /carts/item/:id` -> xoa item

## 5) Cac van de can luu y (quan trong)
1. Home card co dispatch `onAddToCart` nhung `t0` khong dung.
- Hien trang: user bam trong card se di den detail, khong add ngay.
- Neu ky vong add nhanh tai listing, can them nut va goi prop nay trong `t0`.

2. Cart state bi tach doi (`items` va `user_cart.cart_items`).
- Header dem theo `items`, page cart hien theo `user_cart.cart_items`.
- De tranh lech, nen chuan hoa ve 1 nguon du lieu.

3. `removeCartItem` co return theo comma operator.
- Dang su dung mau: `return await Ye.delete(...), {id:n}`
- Van chay, nhung de hieu nham va kho debug.

4. URL anh trong item cart co nguy co loi precedence.
- Mau: `"http://localhost:3000" + x || fallback`
- Neu `x` undefined -> ket qua van la string truthy (`http://localhost:3000undefined`).

## 6) Trinh tu khuyen nghi cho backend/frontend de on dinh
1. Chot 1 endpoint add cart duy nhat (`POST /cart/add` hoac `POST /carts`) va bo fallback.
2. Chuan hoa response item cart, co du `id`, `cart_item_id`, `quantity`, `price`.
3. Dong bo state cart ve 1 nhanh duy nhat (uu tien `user_cart.cart_items` hoac `items`).
4. Bo sung add nhanh o listing neu can UX nhanh.

## 7) Vi tri ma tham chieu
- API axios + interceptors: `dist/assets/index-3SsP24ob.js` (line 16)
- Cart thunks (`Dr`, `Xo`, `Nf`, `Ug`, `Bg`): `dist/assets/index-3SsP24ob.js` (line 16)
- Cart slice (`Hg`): `dist/assets/index-3SsP24ob.js` (line 16)
- Product detail add button (`KE` -> dispatch `Nf`): `dist/assets/index-3SsP24ob.js` (line 16)
- Home listing pass `onAddToCart` (`kE`) + card khong dung (`t0`): `dist/assets/index-3SsP24ob.js` (line 16)
- Entry bundle script: `dist/index.html` (line 8)
