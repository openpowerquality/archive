#ifndef PINCTL_H
#define PINCTL_H

#include <iostream>
#include <fstream>
#include <string>

#define IN "in"
#define OUT "out"
#define LOW "0"
#define HIGH "1"

void writeToFile(std::string value, std::string path);
std::string readValueFromFile(std::string path);
void exportPin(std::string pin);
void unExportPin(std::string pin);
void setPinDirection(std::string pin, std::string direction);
std::string getPinDirection(std::string pin);
void setPinValue(std::string pin, std::string value);
void togglePinValue(std::string pin);
std::string getPinValue(std::string pin);
#endif // PINCTL_H
