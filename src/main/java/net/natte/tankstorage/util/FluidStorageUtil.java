//package net.natte.tankstorage.util;
//
//import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
//import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
//import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
//
//public class FluidStorageUtil {
//    public static Storage<FluidVariant> filteredExtraction(Storage<FluidVariant> fluidStorage,
//            FluidVariant fluidVariantFilter) {
//        return new FilteringStorage<FluidVariant>(fluidStorage) {
//            @Override
//            protected boolean canExtract(FluidVariant resource) {
//                return resource.equals(fluidVariantFilter);
//            }
//        };
//    }
//}
