// CalculatorViewModel.kt

package com.example.stockcalculator.com.example.calculator // 이 ViewModel이 속한 패키지(네임스페이스).

import android.app.Application // AndroidViewModel에서 Application 컨텍스트를 받기 위해 필요.
import androidx.lifecycle.AndroidViewModel // Application을 들고 있을 수 있는 ViewModel(저장소 초기화 등 컨텍스트가 필요할 때 사용).
import kotlinx.coroutines.flow.MutableStateFlow // 내부에서 상태를 “변경 가능”하게 보관하는 Flow(Compose에서 구독하면 자동 UI 갱신).
import kotlinx.coroutines.flow.StateFlow // 외부에 “읽기 전용”으로 노출할 때 쓰는 타입.
import kotlinx.coroutines.flow.asStateFlow // MutableStateFlow -> StateFlow로 안전하게 감싸서 외부에 노출.
import java.time.LocalDate // 이벤트 날짜(LocalDate)를 쓰기 위한 import.
import java.util.UUID // Stock/Preset 등에 고유 id를 만들기 위한 UUID 생성에 사용.

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = StockStorage(application) // 실제 데이터 저장/로드를 담당하는 저장소 객체. 앱 컨텍스트가 필요해서 application 전달.

    private val _stocks = MutableStateFlow<List<Stock>>(emptyList()) // 종목 리스트를 내부 상태로 보관(변경 가능). 초기값은 빈 리스트.
    val stocks: StateFlow<List<Stock>> = _stocks.asStateFlow() // UI에는 읽기 전용으로 노출. UI가 직접 값을 바꾸지 못하게 막음(캡슐화).

    private val _totalAssets = MutableStateFlow(0.0) // 총 자산(종목 평가액 합계)을 내부 상태로 보관. 초기값 0.0.
    val totalAssets: StateFlow<Double> = _totalAssets.asStateFlow() // UI에는 읽기 전용으로 노출.

    private val _assetHistory = MutableStateFlow<List<AssetHistory>>(emptyList()) // 총자산 히스토리(시간, 총자산 값)를 내부 상태로 보관.
    val assetHistory: StateFlow<List<AssetHistory>> = _assetHistory.asStateFlow() // UI에는 읽기 전용으로 노출.

    private val _presets = MutableStateFlow<List<PortfolioPreset>>(emptyList()) // 프리셋 목록(미리 저장한 포트폴리오 구성)을 내부 상태로 보관.
    val presets: StateFlow<List<PortfolioPreset>> = _presets.asStateFlow() // UI에는 읽기 전용으로 노출.

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList()) // 캘린더 이벤트 목록을 내부 상태로 보관.
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow() // UI에는 읽기 전용으로 노출.

    init {
        loadData() // 앱에 저장돼 있던 데이터를 불러와 StateFlow들에 반영.
    }

    private fun loadData() {
        _stocks.value = storage.getStocks() // 저장된 종목 리스트를 불러와 _stocks 상태에 넣음 -> UI가 구독 중이면 자동 갱신.
        _assetHistory.value = storage.getAssetHistory() // 저장된 자산 히스토리를 불러와 상태에 반영.
        _presets.value = storage.getPresets() // 저장된 프리셋 목록을 불러와 상태에 반영.
        _events.value = storage.getEvents() // 저장된 이벤트 목록을 불러와 상태에 반영.
        calculateTotalAssets() // 불러온 종목 리스트 기준으로 총자산을 다시 계산해 _totalAssets에 반영.
    }

    private fun calculateTotalAssets() {
        val total = _stocks.value.sumOf { it.currentValue } // 현재 종목 리스트를 순회하며 currentValue를 합산.
        _totalAssets.value = total // 계산된 합계를 총자산 상태로 반영 -> UI 자동 갱신.
    }

    private fun recordAssetHistory() {
        val currentHistory = _assetHistory.value.toMutableList() // 기존 히스토리(List)를 수정 가능 리스트로 변환(새 항목 추가하기 위해).
        currentHistory.add(AssetHistory(System.currentTimeMillis(), _totalAssets.value)) // (현재시간(ms), 현재 총자산) 기록 1개 추가.
        _assetHistory.value = currentHistory // 갱신된 리스트를 StateFlow에 다시 넣어 UI/구독자에 반영.
        storage.saveAssetHistory(currentHistory) // 히스토리를 영구 저장(앱 재시작해도 유지).
    }

    fun addStock(name: String, targetRatio: Double, currentValue: Double) {
        val newStock = Stock(UUID.randomUUID().toString(), name, targetRatio, currentValue) // 고유 id(UUID)로 새 Stock 객체 생성.
        val updated = _stocks.value + newStock // 기존 리스트에 새 항목을 더해 “새 리스트” 생성(불변 스타일로 안전하게 갱신).
        _stocks.value = updated // 종목 리스트 상태 갱신 -> UI에 즉시 반영.
        storage.saveStocks(updated) // 변경된 종목 리스트를 저장소에 저장(영구 반영).
        calculateTotalAssets() // 종목이 늘었으므로 총자산 재계산.
        recordAssetHistory() // 변경 후 총자산 스냅샷을 히스토리에 기록.
    }

    fun updateStock(stockId: String, name: String, targetRatio: Double, currentValue: Double) {
        val updated = _stocks.value.map { s -> // 현재 리스트를 순회하며 수정 대상만 바꾼 새 리스트 생성.
            if (s.id == stockId) s.copy(name = name, targetRatio = targetRatio, currentValue = currentValue) // id가 같으면 copy로 값만 바꾼 새 객체 생성.
            else s // 수정 대상이 아니면 원래 객체 유지.
        }
        _stocks.value = updated // 수정된 리스트를 상태에 반영 -> UI 갱신.
        storage.saveStocks(updated) // 수정된 리스트를 저장소에 저장.
        calculateTotalAssets() // 평가액이 바뀌었을 수 있으니 총자산 재계산.
        recordAssetHistory() // 변경 후 총자산 스냅샷을 히스토리에 기록.
    }

    fun deleteStock(stockId: String) {
        val updated = _stocks.value.filter { it.id != stockId } // 삭제할 id를 제외한 새 리스트 생성.
        _stocks.value = updated // 삭제 결과를 상태에 반영 -> UI에서 해당 항목 사라짐.
        storage.saveStocks(updated) // 저장소에도 반영.
        calculateTotalAssets() // 종목이 줄었으니 총자산 재계산.
        recordAssetHistory() // 변경 후 총자산 스냅샷 기록.
    }

    fun loadPreset(preset: PortfolioPreset) {
        val newStocks = preset.stocks.map { it.copy(id = UUID.randomUUID().toString()) } // 프리셋 종목들을 가져오되, id를 새로 부여(현재 목록과 충돌 방지).
        _stocks.value = newStocks // 현재 종목 리스트를 프리셋 종목들로 교체.
        storage.saveStocks(newStocks) // 변경된 종목 리스트 저장.
        calculateTotalAssets() // 새 종목 리스트 기준으로 총자산 재계산.
        recordAssetHistory() // 프리셋 적용 후 총자산 스냅샷 기록.
    }

    fun addPreset(name: String, description: String, selectedStocks: List<Stock>) {
        val newPreset = PortfolioPreset(UUID.randomUUID().toString(), name, description, selectedStocks) // 새 프리셋 객체 생성(id는 UUID로 고유하게).
        val updated = listOf(newPreset) + _presets.value // 새 프리셋을 맨 앞에 추가한 새 리스트 생성(최신 프리셋이 위로 오게 하는 의도).
        _presets.value = updated // 프리셋 상태 갱신 -> UI 갱신.
        storage.savePresets(updated) // 저장소에 프리셋 목록 저장.
    }

    fun updatePreset(presetId: String, newName: String, newStocks: List<Stock>) {
        val currentList = _presets.value.toMutableList() // 프리셋 리스트를 수정 가능한 리스트로 변환(특정 인덱스 교체 목적).
        val index = currentList.indexOfFirst { it.id == presetId } // 수정할 프리셋의 위치(인덱스)를 id로 탐색.
        if (index != -1) { // 찾았을 때만 수정(못 찾으면 아무 것도 안 함).
            val updated = currentList[index].copy( // 기존 프리셋을 copy해서 일부만 바꾼 새 객체 생성.
                name = newName, // 프리셋 이름 변경.
                stocks = newStocks, // 프리셋 종목 구성 변경.
                lastModified = System.currentTimeMillis() // 마지막 수정 시간을 현재 시각으로 기록(정렬/표시에 활용 가능).
            )
            currentList[index] = updated // 해당 인덱스의 프리셋을 새 객체로 교체.
            _presets.value = currentList // 상태 갱신 -> UI 갱신.
            storage.savePresets(currentList) // 저장소에도 반영.
        }
    }

    fun deletePreset(presetId: String) {
        val updated = _presets.value.filter { it.id != presetId } // 삭제할 id를 제외한 새 리스트 생성.
        _presets.value = updated // 상태 갱신.
        storage.savePresets(updated) // 저장소에도 반영.
    }

    fun addEvent(title: String, date: LocalDate) {
        val newEvent = CalendarEvent(title = title, date = date) // 새 이벤트 객체 생성(이벤트 id는 CalendarEvent 내부에서 생성될 가능성이 큼).
        val updated = _events.value + newEvent // 기존 이벤트 리스트에 새 이벤트 추가한 새 리스트 생성.
        _events.value = updated // 이벤트 상태 갱신 -> UI 갱신.
        storage.saveEvents(updated) // 저장소에 이벤트 목록 저장.
    }

    fun deleteEvent(event: CalendarEvent) {
        val updated = _events.value.filter { it.id != event.id } // event.id와 같은 것을 제외한 새 리스트 생성.
        _events.value = updated // 상태 갱신.
        storage.saveEvents(updated) // 저장소에도 반영.
    }
}
