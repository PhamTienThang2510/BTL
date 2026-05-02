# TeeZone E-commerce Backend API Architecture

Tài liệu này mô tả kiến trúc thực tế của backend NestJS trong repository fashion-backend. Nội dung được cập nhật theo code hiện tại trong workspace, không dựa trên giả định cũ.

## 1. Tổng quan hệ thống

Hệ thống là một modular monolith dùng NestJS + Prisma + MySQL.

- `src/main.ts` là entry point khởi động app, cấu hình CORS, global validation, Swagger và port lắng nghe.
- `src/app.module.ts` là root module, gom các domain module và static file serving cho thư mục uploads.
- `src/prisma/prisma.service.ts` là lớp truy cập dữ liệu chính qua Prisma Client.
- `prisma/schema.prisma` là nguồn sự thật cho database schema.
- Swagger được expose tại `/api-docs` với bearer auth scheme `JWT-auth`.
- Static files từ `uploads/` được phục vụ qua `/uploads`.

## 2. Runtime và công nghệ

- Framework: NestJS 11
- ORM: Prisma 5 + MySQL
- Auth: JWT qua Passport
- Validation: global `ValidationPipe` với `whitelist: true` và `transform: true`
- Upload: Multer disk storage qua `upload.config.ts`
- HTTP client: axios cho các luồng gọi dịch vụ ngoài, đặc biệt mockup render
- Email: nodemailer cho quên mật khẩu / đặt lại mật khẩu
- Hashing: bcrypt

Scripts đáng chú ý trong `package.json`:

- `npm run start:dev` để chạy dev mode
- `npm run build` để build production
- `npm run test` và `npm run test:e2e` cho test
- `npm run lint` và `npm run format` cho kiểm tra / format mã

## 3. Module map

Các module được đăng ký ở root gồm:

- AuthModule
- UsersModule
- PrismaModule
- ProductsModule
- CategoriesModule
- CartModule
- OrdersModule
- PaymentsModule
- ReviewsModule
- AdminModule
- SellerModule
- UploadModule
- AddressesModule
- MockupsModule
- ServeStaticModule cho `/uploads`

## 4. Route map theo domain

### 4.1 System

- `GET /` trả về chuỗi từ `AppService.getHello()`.

### 4.2 Auth và User

#### `/auth`

- `POST /auth/register`
- `POST /auth/login`

`register` tạo tài khoản người dùng mới; `login` trả về user và JWT access token.

#### `/user`

- `GET /user/me` có `JwtAuthGuard`
- `PUT /user/me` có `JwtAuthGuard`
- `PUT /user/changePassword` có `JwtAuthGuard`
- `POST /user/forgotPassword`
- `POST /user/resetPassword`

Các route `me` và `changePassword` lấy user hiện tại từ `req.user.user_id`.

### 4.3 Danh mục và sản phẩm

#### `/categories`

- `POST /categories`
- `GET /categories`
- `GET /categories/:id`
- `PUT /categories/:id`
- `DELETE /categories/:id`

Theo code hiện tại, controller không gắn guard.

#### `/products`

- `POST /products`
- `GET /products`
- `GET /products/:id`
- `PUT /products/:id`
- `DELETE /products/:id`

`GET /products` có nhận query `categoryId`, `sellerId`, `page`, `limit`, nhưng controller hiện chỉ gọi `productsService.findAll()`.

### 4.4 Reviews

#### `/reviews`

- `POST /reviews`
- `GET /reviews`
- `GET /reviews/:id`
- `PUT /reviews/:id`
- `DELETE /reviews/:id`

Controller hiện không có guard.

### 4.5 Cart

#### `/carts`

- `POST /carts/add`
- `GET /carts/:id`
- `PUT /carts/item/:id`
- `DELETE /carts/item/:id`
- `DELETE /carts/item`

Code hiện tại chưa gắn `JwtAuthGuard`; các thao tác cart vẫn nhận dữ liệu trực tiếp từ body / param.

### 4.6 Orders

#### `/orders`

- `POST /orders` không có guard
- `GET /orders` có `JwtAuthGuard`
- `GET /orders/:id` không có guard
- `PUT /orders/:id` không có guard
- `DELETE /orders/:id` không có guard

Đây là điểm lệch lớn nhất giữa tài liệu cũ và code hiện tại. Hiện tại chỉ route `GET /orders` được bảo vệ, còn các route còn lại đang public trong controller.

### 4.7 Payments

#### `/payments`

- `POST /payments`
- `GET /payments`
- `GET /payments/:id`
- `PUT /payments/:id`
- `DELETE /payments/:id`
- `POST /payments/vnpay-return`

`vnpay-return` là callback xử lý phản hồi VNPay, verify chữ ký rồi cập nhật payment và order payment status. Controller hiện không gắn guard cho toàn bộ payments.

### 4.8 Addresses

#### `/addresses`

Controller này có `@UseGuards(JwtAuthGuard)` ở mức class.

- `POST /addresses`
- `GET /addresses`
- `GET /addresses/provinces`
- `GET /addresses/provinces/:id/wards`
- `GET /addresses/wards/:id`
- `GET /addresses/:id`
- `PATCH /addresses/:id`
- `DELETE /addresses/:id`
- `PATCH /addresses/:id/default`

Các thao tác user-specific đều lấy `req.user.user_id` để ràng buộc ownership.

### 4.9 Seller

#### `/seller`

Controller có `@UseGuards(JwtAuthGuard)` ở mức class.

- `GET /seller/dashboard`
- `GET /seller/products`
- `GET /seller/orders`
- `GET /seller/reviews`
- `GET /seller/wallet`
- `GET /seller/analytics`

### 4.10 Admin

#### `/admin`

- `POST /admin/login`
- `GET /admin/me` có `AdminGuard`

#### `/admin/dashboard`

- `GET /admin/dashboard/stats`

Guard đã được import nhưng đang bị comment trong controller, nên route này hiện chưa thực sự được bảo vệ ở decorator lớp.

#### `/admin/orders`

Controller này có `AdminGuard` ở mức class.

- `GET /admin/orders`
- `GET /admin/orders/:id`
- `PUT /admin/orders/:id/status`

#### `/admin/users`

- `GET /admin/users`
- `PUT /admin/users/:id/status`

Controller hiện không gắn `UseGuards`, dù API docs trong code vẫn mô tả đây là chức năng admin.

### 4.11 Upload

#### `/upload`

- `POST /upload`

Endpoint nhận multipart form-data với field `file`, dùng `FileInterceptor('file', multerConfig)` để ghi file ra disk và trả về filename, path, url, size, mimetype.

### 4.12 Mockups

#### `/mockups/templates`

- `GET /mockups/templates`
- `GET /mockups/templates/:id`

Hai route này public.

#### `/mockups`

- `POST /mockups/render` có `JwtAuthGuard`
- `GET /mockups/renders` có `JwtAuthGuard`
- `GET /mockups/renders/:id` có `JwtAuthGuard`
- `DELETE /mockups/renders/:id` có `JwtAuthGuard`
- `POST /mockups/sync-templates` public trong code hiện tại

`sync-templates` đang được mô tả là thao tác admin, nhưng controller chưa gắn guard.

## 5. Luồng dữ liệu và tích hợp

- Request đi qua Nest controller, DTO validation, service, rồi PrismaService.
- Các luồng xác thực dùng JWT bearer token, lấy user hiện tại từ payload `req.user`.
- Upload lưu file vào `uploads/` trên filesystem và được serve lại qua static route.
- Mockup render gọi dịch vụ ngoài để sinh ảnh rồi lưu trạng thái render vào database.
- Payment VNPay callback xác thực chữ ký, cập nhật payment status, rồi đồng bộ status của order.

## 6. Các điểm cần lưu ý khi đọc code hiện tại

- `orders` đang có nhiều route public ngoài `GET /orders`.
- `admin/dashboard/stats` đang bị comment guard.
- `admin/users` chưa có guard thực tế.
- `mockups/sync-templates` đang public dù có comment nghiệp vụ admin-only.
- Một số controller vẫn nhận `customer_id` hoặc `user_id` từ body/query ở mức API, nên khi chỉnh tiếp cần ràng buộc ownership ở service.

## 7. Tóm tắt ngắn

Backend hiện tại là NestJS modular monolith với Prisma, chia domain rõ theo auth, user, catalog, cart, order, payment, address, seller, admin, upload và mockup. Tài liệu này phản ánh trạng thái code hiện tại, bao gồm cả các lỗ hổng guard đang tồn tại để tiện đối chiếu và ưu tiên sửa.

## 4. Chi tiết endpoint Mockup Render (quan trọng)

### 4.1 POST /mockups/render - Flow chi tiết

**Input (Body):**
```typescript
{
  template_id: number (required) - ID template được chọn
  design_image_url: string (required) - URL file design người dùng upload
  product_id?: number - Gắn render với product cụ thể
  render_config?: {
    smart_object_uuid?: string
    fit?: "fill" | "contain" | "cover"
    color?: string
    image_format?: "webp" | "png" | "jpg"
    image_size?: number
    quality?: number
    brightness?: number
    contrast?: number
    saturation?: number
    opacity?: number
    [key: string]: any
  }
}
```

**Validation & Processing (trong service, theo thứ tự):**

1. ✅ Kiểm tra template tồn tại
2. ✅ Kiểm tra design_image_url không rỗng
3. ✅ HEAD request design_image_url (timeout 5s)
4. ✅ Tạo record mockup_renders:
   - customer_id = req.user.user_id (từ JWT)
   - product_id = body.product_id (nullable)
   - template_id = body.template_id
   - design_image_url = body.design_image_url
   - render_config = JSON.stringify(body.render_config)
   - status = "processing"
   
5. ✅ Build smart_objects cho Sudomock:
   ```
   - Nếu template.smart_objects là array: map thành { uuid, asset }
   - Nếu không: tạo 1 smart object mặc định
   - asset.url = design_image_url
   - asset.fit = render_config.fit || 'fill'
   - asset.size = { width: 672, height: 610 }
   - asset.position = { top: 50, left: 50 }
   - asset.rotate = 0
   ```

6. ✅ Gọi Sudomock API:
   ```
   POST https://api.sudomock.com/api/v1/renders
   {
     mockup_uuid: template.mockup_uuid
     smart_objects: [...]
     export_options: {
       image_format: render_config.image_format || 'webp'
       image_size: render_config.image_size || 1920
       quality: render_config.quality || 95
     }
   }
   ```

7. ✅ Xử lý response từ Sudomock:
   - Nếu thành công:
     - rendered_image_url = response.data.data.print_files[0].export_path
     - status = "completed"
     - Trả về record đã update
   - Nếu lỗi:
     - status = "failed"
     - error_message = '' (hiện để rỗng, cần cải thiện)
     - Throw 500 InternalServerErrorException

**Output (Success):**
```json
{
  render_id: 1,
  customer_id: 1,
  product_id: 6,
  template_id: 1,
  design_image_url: "/uploads/design-1234.png",
  rendered_image_url: "https://sudomock-storage.../file.webp",
  render_config: "{...}",
  status: "completed",
  error_message: null,
  created_at: "2024-04-28T10:30:00Z",
  updated_at: "2024-04-28T10:31:00Z",
  mockup_templates: { /* template details */ }
}
```

**Ý nghĩa các trường:**
- `rendered_image_url` - Output cuối cùng để FE hiển thị preview/add-to-cart
- `render_id` - Khóa liên kết xuống cart_items.render_id và order_items.render_id
- `render_config` - Lưu dạng string JSON (không phải JSON typed column)

### 4.2 Trạng thái Render

Enum `mockup_status`:
- `pending` - Mới tạo, chưa xử lý
- `processing` - Đang gọi Sudomock API
- `completed` - Render thành công
- `failed` - Render thất bại

## 5. Authentication & Authorization (AuthN/AuthZ)

### 5.1 JWT User Flow

**Registration → Login → Protected Endpoints**

1. **POST /auth/register** (public)
   - Nhập email, password, full_name, phone_number
   - Hash password với bcrypt
   - Tạo users record (role = CUSTOMER)
   - Tạo customers record liên kết
   - Tạo carts record
   - Return access_token

2. **JWT Token Structure**
   ```json
   {
     "sub": user_id,
     "email": "user@example.com",
     "role": "CUSTOMER",
     "iat": timestamp,
     "exp": timestamp
   }
   ```
   - Secret: JWT_SECRET từ .env
   - TTL: Configurable

3. **JwtAuthGuard Flow**
   - Trích Bearer token từ Authorization header
   - JwtStrategy validate token
   - AuthService.validateUser(payload) kiểm tra user tồn tại
   - req.user được populate với decoded payload

### 5.2 JWT Admin Flow

1. **POST /admin/login** (public)
   - Email + password
   - Chỉ chấp nhận role ADMIN
   - Return access_token

2. **AdminGuard Flow**
   - Kế thừa JwtAuthGuard
   - Kiểm tra role = 'ADMIN'
   - Throw ForbiddenException nếu không phải admin

### 5.3 Guard Decorator Summary

| Endpoint | Guard | Status | Note |
|----------|-------|--------|------|
| /user/me, /user/* | JwtAuthGuard | ✅ OK | Protected |
| /orders GET (user) | JwtAuthGuard | ✅ OK | Protected |
| /orders POST, PUT, DELETE | ❌ None | ⚠️ ISSUE | Public |
| /addresses/* | Class-level JwtAuthGuard | ✅ OK | Protected |
| /seller/* | Class-level JwtAuthGuard | ✅ OK | Protected |
| /mockups/render | JwtAuthGuard | ✅ OK | Protected (cần kiểm tra role) |
| /mockups/renders/* | JwtAuthGuard | ✅ OK | Protected |
| /admin/dashboard/stats | ❌ Commented | ⚠️ CRITICAL | Public (nên là AdminGuard) |
| /admin/orders/* | AdminGuard | ✅ OK | Protected |
| /admin/users GET/PUT | ❌ None | ⚠️ ISSUE | Public (nên là AdminGuard) |
| /mockups/sync-templates | ❌ None | ⚠️ CRITICAL | Public (nên là AdminGuard) |
| /categories, /products, /reviews, /payments | ❌ None | ❓ Depends | Nên review yêu cầu nghiệp vụ |

## 6. Kiến trúc dữ liệu (Prisma)

### 6.1 Nhóm bảng chính

**Identity & Roles:**
- `users` - Tài khoản hệ thống (ADMIN, SELLER, CUSTOMER)
- `customers` - Thông tin khách hàng (1-1 với users)
- `sellers` - Thông tin người bán (1-1 với users)

**Catalog:**
- `categories` - Danh mục sản phẩm
- `products` - Sản phẩm chính
- `product_variants` - Biến thể sản phẩm (size, color, etc.)
- `product_media` - Hình ảnh/media sản phẩm

**Commerce:**
- `carts` - Giỏ hàng
- `cart_items` - Item trong giỏ (liên kết product_variants, mockup_renders)
- `orders` - Đơn hàng
- `order_items` - Chi tiết order (liên kết product_variants, mockup_renders)
- `payments` - Thanh toán
- `shipments` - Vận chuyển
- `refunds` - Hoàn tiền
- `cancel_requests` - Yêu cầu hủy

**Promotion & Wallet:**
- `promotions` - Mã khuyến mãi
- `promotion_usages` - Sử dụng mã khuyến mãi
- `wallets` - Ví tiền (loyalty points)
- `wallet_transactions` - Giao dịch ví

**Reviews:**
- `reviews` - Bài đánh giá sản phẩm
- `review_media` - Hình ảnh trong review

**Address:**
- `customer_addresses` - Địa chỉ giao hàng
- `provinces` - Tỉnh/thành phố
- `wards` - Phường/xã

**Print-on-Demand/Mockup:**
- `mockup_templates` - Template mockup (có template_id, mockup_uuid, smart_objects)
- `mockup_renders` - Kết quả render (render_id, status, rendered_image_url)

### 6.2 Quan hệ nghiệp vụ quan trọng

```
users (1) ─┬─ (1) customers
           └─ (1) sellers

customers (1) ─── (n) carts
customers (1) ─── (n) orders
customers (1) ─── (n) reviews
customers (1) ─── (n) customer_addresses
customers (1) ─── (n) mockup_renders

products (1) ─── (n) product_variants
products (1) ─── (n) product_media
products (1) ─── (n) mockup_renders

orders (1) ─┬─ (n) order_items
            ├─ (n) payments
            └─ (n) shipments

order_items (1) ─── (?) mockup_renders [render_id nullable, SetNull on delete]
cart_items (1) ─── (?) mockup_renders [render_id nullable, SetNull on delete]

mockup_templates (1) ─── (n) mockup_renders
product_variants (1) ─── (n) mockup_templates [variant_id nullable]
```

### 6.3 Enums chính

```
enum users_role {
  ADMIN
  SELLER
  CUSTOMER
}

enum users_status {
  active
  inactive
}

enum mockup_status {
  pending
  processing
  completed
  failed
}

enum product_media_media_type {
  image (default)
  video
}

// và các enum khác cho refund status, payment status, order status, etc.
```

## 7. Luồng nghiệp vụ chính

### 7.1 Đăng ký tài khoản Customer

```
POST /auth/register
  ↓ [Hash password + Create users]
  ↓ [Create customers + carts]
  ↓ Return { user, access_token }
```

### 7.2 Mua hàng cơ bản

```
1. GET /categories → GET /products/:id
2. POST /carts/add (customer_id, variant_id, quantity)
3. GET /carts/:id (review items)
4. POST /orders (customer_id, address_id, payment_method)
   ├─ Create orders record
   └─ Create order_items từ cart_items
5. POST /payments (order_id, amount, payment_method)
6. GET /orders/:id (track order status)
```

**Lưu ý:** Nếu dùng mockup product:
- POST /mockups/render → render_id
- POST /carts/add (cart_items.render_id = render_id)
- POST /orders (order_items.render_id = render_id)

### 7.3 Luồng Mockup/Print-on-Demand

```
1. GET /mockups/templates (danh sách templates)
2. POST /mockups/render (template_id + design_image_url) [JwtAuthGuard]
   ├─ Validate template & design URL
   ├─ Create mockup_renders (status=processing)
   ├─ Call Sudomock API /api/v1/renders
   └─ Update rendered_image_url (status=completed)
3. GET /mockups/renders (render history của user)
4. Sử dụng render_id trong cart/order
```

**Sudomock Integration:**
- Gọi: `POST https://api.sudomock.com/api/v1/renders`
- Input: mockup_uuid, smart_objects[], export_options
- Output: print_files[0].export_path
- Timeout: 5s per request

### 7.4 Luồng Seller Analytics

```
GET /seller/dashboard
  ├─ Query: orders có order_items.product_id từ seller
  ├─ Tính: totalSales, totalOrders, revenue_this_month
  ├─ Query: reviews cho products của seller
  └─ Return: { totalSales, totalOrders, averageRating, ... }
```

⚠️ **Performance Issue**: Hiện logic filter trong memory (JavaScript),
should move to SQL WHERE clause khi data lớn.

### 7.5 Luồng Admin Dashboard

```
GET /admin/dashboard/stats [❌ Guard commented]
  ├─ Query: totalSales = SUM(orders.total_amount)
  ├─ Query: totalOrders = COUNT(orders)
  ├─ Query: totalUsers = COUNT(users where role=CUSTOMER)
  ├─ Query: activeProducts = COUNT(products where status=active)
  └─ Return: comprehensive stats
```

## 8. Tích hợp bên ngoài & Environment Variables

### 8.1 Environment Variables cần có

```bash
# Database
DATABASE_URL=mysql://user:password@localhost:3306/teezone

# Server
PORT=3000
NODE_ENV=development|production

# JWT
JWT_SECRET=your-secret-key
JWT_RESET_SECRET=your-reset-secret

# Email (nodemailer)
EMAIL_USER=noreply@teezone.com
EMAIL_PASSWORD=app-password

# Frontend
FRONTEND_URL=http://localhost:5173

# Sudomock API (MockUp/POD)
SUDOMOCK_API_KEY=your-api-key
SUDOMOCK_API_URL=https://api.sudomock.com

# Payment Gateway (VNPay)
VNPAY_TMN_CODE=your-tmn-code
VNPAY_SECRET_KEY=your-secret-key
VNPAY_RETURN_URL=http://localhost:3000/payments/vnpay-return
```

### 8.2 Tích hợp Outbound

**1. Sudomock API**
- Purpose: Render mockup images (POD)
- Endpoint: `POST /api/v1/renders`
- Timeout: 5 seconds
- Retry: Hiện không có (cần thêm)
- Error Handling: Set status=failed, error_message (currently empty)

**2. VNPay Payment Gateway**
- Purpose: Online payment processing
- Flow: Redirect to VNPay → Return callback → Verify signature
- Verify: HMAC SHA512
- Integration: PaymentsService.verifyVnpayCallback()

**3. SMTP/Nodemailer**
- Purpose: Send forgot/reset password emails
- Provider: Gmail / Email service
- Template: Plain text or HTML
- Integration: AuthService.sendResetEmail()

## 9. Vấn đề kỹ thuật hiện tại (⚠️ Priority Issues)

### 🔴 Critical Security Issues

1. **Admin Dashboard Guard Commented Out**
   - File: [src/admin/admin-dashboard.controller.ts](src/admin/admin-dashboard.controller.ts#L12)
   - Current: `// @UseGuards(AdminGuard)` (commented)
   - Issue: `/admin/dashboard/stats` endpoint is PUBLIC
   - Impact: Anyone can access admin statistics
   - Fix: Uncomment `@UseGuards(AdminGuard)`

2. **Mockup Sync Templates - No Auth**
   - File: [src/mockups/mockups.controller.ts](src/mockups/mockups.controller.ts#L306)
   - Current: No `@UseGuards()` decorator
   - Issue: `POST /mockups/sync-templates` endpoint is PUBLIC
   - Impact: Anyone can modify mockup template metadata
   - Fix: Add `@UseGuards(AdminGuard)` - Admin only operation

3. **Admin Users Controller - No Guard**
   - File: [src/admin/admin-users.controller.ts](src/admin/admin-users.controller.ts#L18)
   - Current: `@Controller('admin/users')` without `@UseGuards(AdminGuard)`
   - Issue: `GET /admin/users` and `PUT /admin/users/:id/status` are PUBLIC
   - Impact: Anyone can view/modify user statuses
   - Fix: Add class-level `@UseGuards(AdminGuard)`

### 🟡 Authorization Issues

4. **CRUD Operations Missing Guards**
   - `/categories/*` (POST, PUT, DELETE) - No guard
   - `/products/*` (POST, PUT, DELETE) - No guard
   - `/payments/*` (all methods) - No guard
   - `/reviews/*` (all methods) - No guard
   - `/orders` (POST, PUT, DELETE) - No guard (only GET has guard)
   - **Action**: Review business requirements and add appropriate guards

5. **Mockup Render Role Validation Missing**
   - File: [src/mockups/mockups.controller.ts](src/mockups/mockups.controller.ts#L116)
   - Issue: `POST /mockups/render` has JwtAuthGuard but doesn't verify role=CUSTOMER
   - Risk: SELLER/ADMIN could also render mockups if data exists
   - Fix: Add RoleGuard or explicit role check

### 🟠 Data & Error Handling Issues

6. **Empty Error Messages in Mockup Render**
   - File: [src/mockups/mockups.service.ts](src/mockups/mockups.service.ts) (assumed)
   - Current: When Sudomock API fails, `error_message` is set to empty string
   - Impact: No debugging info when render fails
   - Fix: Capture actual error from Sudomock and log it

7. **Smart Objects Type Mismatch**
   - Schema: `mockup_templates.smart_objects` is stored as `String (LongText)`
   - Code: Service treats it as `array` (parse/stringify needed)
   - Issue: No explicit parse/stringify utility, prone to errors
   - Fix: Create dedicated utility or use JSON column type if MySQL supports

8. **Render Config Data Type**
   - Current: `mockup_renders.render_config` stored as `String (LongText)`
   - Should: Either use JSON column or have explicit serialization utility
   - Fix: Add `parseRenderConfig()` utility function

### 🔵 Performance Issues

9. **Seller Service Memory Filter**
   - File: [src/seller/seller.service.ts](src/seller/seller.service.ts) (assumed)
   - Current: Query all orders, then filter in JavaScript by seller_id
   - Issue: O(n) in memory, scales poorly with large datasets
   - Fix: Move WHERE clause to Prisma/SQL query

10. **Admin Dashboard Full Table Scans**
    - Current: COUNT() all users/orders without WHERE clause
    - Should: Add indexes, partition data by date ranges
    - Fix: Optimize queries with WHERE conditions and indexes

### 📋 Logging & Monitoring

11. **Generic Error Throwing**
    - Issue: Many endpoints throw generic `new Error()` or `InternalServerErrorException`
    - Impact: Hard to debug issues in production
    - Fix: Implement structured logging with context (user_id, endpoint, error details)

12. **Missing Validation in DTOs**
    - Some CreateXxxDto classes may be missing validation decorators
    - Fix: Add @IsEmail(), @IsNotEmpty(), @IsNumber() etc. from class-validator

## 10. Hướng dẫn cải thiện hệ thống

### Ưu tiên ngắn hạn (Sprint gần)

1. **Fix Critical Security Issues** (1-2 ngày)
   - [ ] Uncomment AdminGuard in dashboard controller
   - [ ] Add AdminGuard to /admin/users endpoints
   - [ ] Add AdminGuard to /mockups/sync-templates

2. **Fix Authorization Issues** (2-3 ngày)
   - [ ] Add guards to unprotected CRUD operations
   - [ ] Add role validation to mockup render
   - [ ] Add role-based access control (RoleGuard)

3. **Fix Data Handling** (2-3 ngày)
   - [ ] Capture error_message from Sudomock API
   - [ ] Create smart_objects parse/stringify utility
   - [ ] Add explicit render_config serialization

### Ưu tiên trung hạn (1-2 sprint)

4. **Performance Optimization** (3-5 ngày)
   - [ ] Move seller filtering to SQL WHERE clause
   - [ ] Add database indexes for common queries
   - [ ] Implement pagination for list endpoints
   - [ ] Cache frequently accessed data (categories, templates)

5. **Logging & Monitoring** (3-5 ngày)
   - [ ] Implement structured logging (winston/pino)
   - [ ] Add request/response logging middleware
   - [ ] Add error tracking (Sentry/DataDog)
   - [ ] Metrics for API performance (response time, error rate)

6. **Testing Coverage** (5-7 ngày)
   - [ ] Unit tests for auth flows (register, login, reset password)
   - [ ] Integration tests for cart → order → payment flow
   - [ ] E2E tests for mockup render flow
   - [ ] Authorization tests (verify guards work correctly)

### Ưu tiên dài hạn (Refactor)

7. **Database Schema Improvements**
   - Use JSON column for smart_objects, render_config
   - Add computed columns for frequently aggregated data
   - Partition large tables by date (orders)

8. **API Versioning**
   - Plan for v2 API with backward compatibility
   - Deprecate old endpoints gracefully

9. **Rate Limiting & Throttling**
   - Implement per-user rate limits
   - Prevent abuse of expensive operations (render, upload)

10. **Caching Strategy**
    - Redis cache for user sessions
    - Cache template list (TTL: 1 hour)
    - Cache seller analytics (TTL: 5 minutes)
