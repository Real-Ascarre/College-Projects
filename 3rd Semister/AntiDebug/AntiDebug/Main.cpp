#include <windows.h>
#include <TlHelp32.h>
#include <tchar.h>
#include <iostream>

using namespace std;

#define INSN_SOFTWARE_BREAKPOINT    0xCC

int GetProcessID(const char* module_name) {
    int pid = 0;
    int threadCount = 0;
    HANDLE hSnap = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
    PROCESSENTRY32 pe;
    pe.dwSize = sizeof(PROCESSENTRY32);
    Process32First(hSnap, &pe);
    while (Process32Next(hSnap, &pe)) {
        if (_tcsicmp(pe.szExeFile, _T(module_name)) == 0) {
            if ((int)pe.cntThreads > threadCount) {
                threadCount = pe.cntThreads;
                pid = pe.th32ProcessID;
            }
        }
    }

    return pid;
}

BOOL AduRevertPatchNtdllDbgBreakPoint(HANDLE hProcess) {
    HMODULE hNtdll = NULL;
    PVOID pDbgBreakPoint = NULL;
    BYTE Instruction = 0;
    DWORD PreviousProtection = 0;
    BOOLEAN fRestorePageProtection = FALSE;
    BYTE SoftwareBreakpointInstruction = 0xCC;
    BOOL status = TRUE;

    cout << "Reverting ntdll.DbgBreakPoint patch." << endl;
	
	//Get Ntdll base address
    hNtdll = GetModuleHandleW(L"ntdll.dll");
    if (!hNtdll) {
        cout << "GetModuleHandleW failed: " << GetLastError() << endl;
        status = FALSE;
        goto exit;
    }

	//Get Function address
    pDbgBreakPoint = GetProcAddress(hNtdll, "DbgBreakPoint");
    if (!pDbgBreakPoint){
        cout << "GetProcAddress failed: " << GetLastError() << endl;
        status = FALSE;
        goto exit;
    }

    cout << "Found ntdll.DbgBreakPoint: " << pDbgBreakPoint << endl;
	
	//Read the function from program memory
    status = ReadProcessMemory(hProcess, pDbgBreakPoint, &Instruction, sizeof(Instruction), NULL);
    if (!status) {
        cout << "ReadProcessMemory failed: " << GetLastError() << "lpBaseAddress = " << pDbgBreakPoint << endl;
        goto exit;
    }

	//Check if breakpoint is already 0xCC
    if (INSN_SOFTWARE_BREAKPOINT == Instruction) {
        cout << "ntdll.DbgBreakPoint is currently '0xCC'." << endl;
        goto exit;
    }

    cout << "ntdll.DbgBreakPoint is currently at " << Instruction << "patching it to '0xCC'." << endl;

    // Patch the region to be wwritable
    status = VirtualProtectEx(hProcess,pDbgBreakPoint,sizeof(Instruction),PAGE_EXECUTE_READWRITE, &PreviousProtection);
    if (!status)
    {
        cout <<
            "VirtualProtectEx failed: " << GetLastError() << "lpAddress = " << pDbgBreakPoint << "flNewProtect = 0x" << PAGE_EXECUTE_READWRITE << endl;
        goto exit;
    }

    fRestorePageProtection = TRUE;

	//Write the function to 0xCC to allow Breakpoints for Debugger
    status = WriteProcessMemory(hProcess,pDbgBreakPoint,&SoftwareBreakpointInstruction,sizeof(SoftwareBreakpointInstruction),NULL);
    if (!status){
        cout << "WriteProcessMemory failed: " << GetLastError() << "lpBaseAddress = " << pDbgBreakPoint << endl;
        goto exit;
    }

exit:
    if (fRestorePageProtection){
        BOOL UnwindStatus = VirtualProtectEx(hProcess,pDbgBreakPoint,sizeof(Instruction),PreviousProtection,&PreviousProtection);
        if (!UnwindStatus){
            cout <<
                "VirtualProtectEx failed: " << GetLastError() << "lpBaseAddress = " << pDbgBreakPoint << "flNewProtect = 0x" << PreviousProtection << endl;
        }
    }

    return status;
}

void main() {
	//Get Target Process Id
    int ProcessId = GetProcessID("notepad.exe");
	
	//Open handle to target process
    HANDLE ProcessHandle = OpenProcess(PROCESS_ALL_ACCESS, NULL, ProcessId);

    BOOL status = TRUE;
    status = AduRevertPatchNtdllDbgBreakPoint(ProcessHandle);
    if (!status) {
        cout << "Failed" << endl;
    }
}