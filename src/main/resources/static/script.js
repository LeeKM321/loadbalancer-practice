// 전역 변수
let requestCounter = 0;

// 유틸리티 함수
function formatJSON(data) {
    return JSON.stringify(data, null, 2);
}

function formatTimestamp() {
    return new Date().toLocaleString('ko-KR');
}

function showLoading(elementId) {
    const element = document.getElementById(elementId);
    element.innerHTML = '<div class="loading"></div>요청 처리 중...';
}

function displayResult(elementId, data, requestInfo = '') {
    const element = document.getElementById(elementId);
    const timestamp = formatTimestamp();
    const headers = data.headers || {};

    let result = `[${timestamp}] 요청 #${++requestCounter}\n`;
    if (requestInfo) {
        result += `${requestInfo}\n`;
    }
    result += `=== 응답 헤더 ===\n`;

    // 주요 헤더 정보 표시
    if (headers['x-cache-status']) {
        result += `X-Cache-Status: ${headers['x-cache-status']}\n`;
    }
    if (headers['x-upstream-response-time']) {
        result += `X-Upstream-Response-Time: ${headers['x-upstream-response-time']}\n`;
    }
    if (headers['server']) {
        result += `Server: ${headers['server']}\n`;
    }

    result += `\n=== 응답 데이터 ===\n`;
    result += formatJSON(data.body);
    result += `\n${'='.repeat(50)}\n\n`;

    element.innerHTML += result;
    element.scrollTop = element.scrollHeight;
}

function displayError(elementId, error, requestInfo = '') {
    const element = document.getElementById(elementId);
    const timestamp = formatTimestamp();

    let result = `[${timestamp}] 오류 발생\n`;
    if (requestInfo) {
        result += `${requestInfo}\n`;
    }
    result += `오류: ${error.message}\n`;
    result += `${'='.repeat(50)}\n\n`;

    element.innerHTML += result;
    element.scrollTop = element.scrollHeight;
}

// API 호출 함수
async function makeRequest(url, method = 'GET', body = null) {
    try {
        const options = {
            method: method,
            headers: {
                'Content-Type': 'application/json',
            }
        };

        if (body) {
            options.body = JSON.stringify(body);
        }

        const response = await fetch(url, options);
        const responseData = await response.json();

        // 응답 헤더를 객체로 변환
        const headers = {};
        response.headers.forEach((value, key) => {
            headers[key] = value;
        });

        return {
            status: response.status,
            statusText: response.statusText,
            headers: headers,
            body: responseData
        };
    } catch (error) {
        throw new Error(`네트워크 오류: ${error.message}`);
    }
}

// 로드 밸런서 테스트 함수
async function testLoadBalancer() {
    showLoading('loadBalancerTest');
    try {
        const result = await makeRequest('/api/lb');
        displayResult('loadBalancerTest', result, 'GET /api/lb - 로드 밸런서 테스트');
    } catch (error) {
        displayError('loadBalancerTest', error, 'GET /api/lb - 로드 밸런서 테스트');
    }
}

async function testMultipleRequests() {
    const element = document.getElementById('loadBalancerTest');
    element.innerHTML += `[${formatTimestamp()}] 연속 요청 테스트 시작 (10회)\n`;

    for (let i = 1; i <= 10; i++) {
        try {
            element.innerHTML += `요청 ${i}/10 처리 중...\n`;
            const result = await makeRequest('/api/lb');
            displayResult('loadBalancerTest', result, `연속 요청 ${i}/10 - GET /api/lb`);

            // 요청 간 짧은 지연
            await new Promise(resolve => setTimeout(resolve, 200));
        } catch (error) {
            displayError('loadBalancerTest', error, `연속 요청 ${i}/10 - GET /api/lb`);
        }
    }

    element.innerHTML += `[${formatTimestamp()}] 연속 요청 테스트 완료\n${'='.repeat(50)}\n\n`;
}

// 사용자 API 테스트 함수 (캐싱)
async function fetchUsers() {
    showLoading('usersTest');
    try {
        const result = await makeRequest('/api/users');
        displayResult('usersTest', result, 'GET /api/users - 사용자 목록 조회 (캐싱 적용)');
    } catch (error) {
        displayError('usersTest', error, 'GET /api/users - 사용자 목록 조회');
    }
}

async function fetchUser(id) {
    showLoading('usersTest');
    try {
        const result = await makeRequest(`/api/users/${id}`);
        displayResult('usersTest', result, `GET /api/users/${id} - 사용자 조회 (캐싱 적용)`);
    } catch (error) {
        displayError('usersTest', error, `GET /api/users/${id} - 사용자 조회`);
    }
}

async function createUser() {
    showLoading('usersTest');
    try {
        const userData = {
            name: `테스트사용자${Date.now()}`,
            email: `test${Date.now()}@example.com`,
            department: '테스트팀'
        };

        const result = await makeRequest('/api/users', 'POST', userData);
        displayResult('usersTest', result, 'POST /api/users - 새 사용자 생성');
    } catch (error) {
        displayError('usersTest', error, 'POST /api/users - 새 사용자 생성');
    }
}

// 주문 API 테스트 함수 (실시간)
async function fetchOrders() {
    showLoading('ordersTest');
    try {
        const result = await makeRequest('/api/orders');
        displayResult('ordersTest', result, 'GET /api/orders - 주문 목록 조회 (실시간 데이터)');
    } catch (error) {
        displayError('ordersTest', error, 'GET /api/orders - 주문 목록 조회');
    }
}

async function fetchOrder(id) {
    showLoading('ordersTest');
    try {
        const result = await makeRequest(`/api/orders/${id}`);
        displayResult('ordersTest', result, `GET /api/orders/${id} - 주문 조회 (실시간 데이터)`);
    } catch (error) {
        displayError('ordersTest', error, `GET /api/orders/${id} - 주문 조회`);
    }
}

async function createOrder() {
    showLoading('ordersTest');
    try {
        const orderData = {
            userId: Math.floor(Math.random() * 3) + 1,
            productName: `테스트상품${Date.now()}`,
            quantity: Math.floor(Math.random() * 5) + 1,
            price: Math.floor(Math.random() * 100000) + 10000
        };

        const result = await makeRequest('/api/orders', 'POST', orderData);
        displayResult('ordersTest', result, 'POST /api/orders - 새 주문 생성');
    } catch (error) {
        displayError('ordersTest', error, 'POST /api/orders - 새 주문 생성');
    }
}

// 캐시 상태 테스트
async function testCacheStatus() {
    const sections = ['loadBalancerTest', 'usersTest', 'ordersTest'];
    sections.forEach(section => {
        const element = document.getElementById(section);
        element.innerHTML += `\n[${formatTimestamp()}] === 캐시 상태 테스트 시작 ===\n`;
    });

    // 사용자 API 연속 호출 (캐시 테스트)
    document.getElementById('usersTest').innerHTML += `사용자 API 연속 호출 (캐시 HIT/MISS 확인):\n`;
    for (let i = 1; i <= 3; i++) {
        try {
            const result = await makeRequest('/api/users');
            displayResult('usersTest', result, `캐시 테스트 ${i}/3 - GET /api/users`);
            await new Promise(resolve => setTimeout(resolve, 500));
        } catch (error) {
            displayError('usersTest', error, `캐시 테스트 ${i}/3 - GET /api/users`);
        }
    }

    // 주문 API 연속 호출 (캐시 없음 확인)
    document.getElementById('ordersTest').innerHTML += `주문 API 연속 호출 (캐시 적용 안됨 확인):\n`;
    for (let i = 1; i <= 3; i++) {
        try {
            const result = await makeRequest('/api/orders');
            displayResult('ordersTest', result, `실시간 데이터 테스트 ${i}/3 - GET /api/orders`);
            await new Promise(resolve => setTimeout(resolve, 500));
        } catch (error) {
            displayError('ordersTest', error, `실시간 데이터 테스트 ${i}/3 - GET /api/orders`);
        }
    }
}

// 결과 초기화
function clearResults() {
    const resultBoxes = document.querySelectorAll('.result-box');
    resultBoxes.forEach(box => {
        box.innerHTML = '';
    });
    requestCounter = 0;
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    console.log('Nginx 로드 밸런서 테스트 페이지가 로드되었습니다.');
    console.log('브라우저 개발자 도구의 Network 탭에서 요청/응답 헤더를 확인할 수 있습니다.');
});
