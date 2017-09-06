#include "pinctl.hpp"
#include <iostream>
#include <fstream>
#include <string>

void writeToFile(std::string value, std::string path)
{
    std::ofstream f;
    f.open(path.c_str());
    f << value;
    f.close();
}

std::string readValueFromFile(std::string path)
{
    std::string value;
    std::ifstream f;
    f.open(path.c_str());
    if(f.is_open())
    {
        std::getline(f, value);
    }
    return value;
}

void exportPin(std::string pin)
{
    writeToFile(pin, "/sys/class/gpio/export");
}

void unExportPin(std::string pin) {
    writeToFile(pin, "/sys/class/gpio/unexport");
}

void setPinDirection(std::string pin, std::string direction)
{
    writeToFile(direction, "/sys/class/gpio/gpio" + pin + "/direction");
}

std::string getPinDirection(std::string pin)
{
    return readValueFromFile("/sys/class/gpio/gpio" + pin + "/direction");
}

void setPinValue(std::string pin, std::string value)
{
    writeToFile(value, "/sys/class/gpio/gpio" + pin + "/value");
}

std::string getPinValue(std::string pin)
{
    return readValueFromFile("/sys/class/gpio/gpio" + pin + "/value");
}

void togglePinValue(std::string pin)
{
    setPinValue(pin, (getPinValue(pin) == HIGH ? LOW : HIGH));
}

int main(int argc, char *argv[])
{
    if(argc < 3) 
    {
        std::cout << "Usage: ./pinctl [cmd] [pin]\n";
        std::cout << "where [cmd] is one of export, unexport, in, out, high, low, toggle, val\n";
        return 0;
    }

    std::string cmd = argv[1];
    std::string pin = argv[2];
    
    if(cmd == "export")
    {
       exportPin(pin); 
    }
    if(cmd == "unexport")
    {
      unExportPin(pin);
    }
    if(cmd == "in")
    {
        setPinDirection(pin, IN);
    }
    if(cmd == "out")
    {
        setPinDirection(pin, OUT);
    }
    if(cmd == "high")
    {
        setPinValue(pin, HIGH);
    }
    if(cmd == "low")
    {
        setPinValue(pin, LOW);
    }
    if(cmd == "toggle")
    {
        togglePinValue(pin);
    }
    if(cmd == "val?")
    {
        std::cout << getPinValue(pin) << "\n";
    }
    if(cmd == "dir?")
    {
        std::cout << getPinDirection(pin) << "\n";
    }
    
    return 0;
}
